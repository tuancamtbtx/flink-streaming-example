#!/bin/bash

set -e
set -x

USAGE="
Usage: $(basename "$0") <cmd>
Deploy helm chart
Available options:
  deploy <deployment> <path_to_values.yaml> <application args>
  template <deployment> <path_to_values.yaml> <application args>
  clean <deployment> => remove the job completely
"
FLINK_DEPLOYMENT_REPO="${FLINK_DEPLOYMENT_REPO:-}"
DEPLOY_IMAGE_NAME="${DEPLOY_IMAGE_NAME:-}"
DEPLOY_IMAGE_TAG="${DEPLOY_IMAGE_TAG:-}"
TAIL_LOG_TIMEOUT=${TAIL_LOG_TIMEOUT:-300}

_script_directory() {
  local base
  base=$(dirname $0)

  [ -z "$base" ] && base="."
  (cd "$base" && pwd)
}

SCRIPT_DIRECTORY=$(_script_directory)

_check_deploy_exist() {
  kubectl describe deployment "$1" >/dev/null
}

wait_until_done() {
  p=$1
  exit_code=$2
  cnt=${TAIL_LOG_TIMEOUT}
  origcnt=$cnt
  while kill -0 $p >/dev/null 2>&1; do
    if [ $cnt -gt 1 ]; then
      cnt=$(expr $cnt - 1)
      sleep 1
    else
      echo "Process did not complete after $origcnt seconds, exit with code: $exit_code."
      kill -9 $p
      exit $exit_code
    fi
  done
  return 0
}

_parse_args_flag() {
  local job_args=""
  local is_set_value=0
  local set_flag=""
  local set_flag_name=""
  arr=("$@")
  for arg in "${arr[@]}"; do
    if [[ "$arg" == "--set" || "$arg" == "--set-string" ]]; then
      is_set_value=1
      set_flag_name="$arg"
      continue
    fi

    ## handle flinkConfiguration
    if [[ $arg == flinkConfiguration* ]]; then
      # remove 'flinkConfiguration.' and escape '.'
      local escaped_options=$(echo "$arg" | sed -e "s/flinkConfiguration\.//g" | sed -e "s/\./\\\./g")
      # replace '=' with '"='
      escaped_options=$(echo $escaped_options | sed -e "s/=/\\\"=/g")
      arg="flinkConfiguration.\"$escaped_options"
    fi

    if [[ "$is_set_value" == "1" ]]; then
      set_flag=$(echo $set_flag $set_flag_name $arg)
      is_set_value=0
      continue
    fi

    job_args="${job_args},${arg}"
  done

  job_args=$(echo $job_args | sed -e "s/^,//g")

  echo "${set_flag} --set job.args='{$job_args}'"
}

cmd_template() {
  local deployment=$1
  shift || error "Missing deployment. $USAGE"
  if [[ -z "$deployment" ]]; then
    error "Deployment is empty."
  fi

  local values_path=$1
  shift || error "Missing helm values.yaml. $USAGE"
  [ -f "$values_path" ] || error "Values path does not exist."

  local helm_cmd=$(
    echo helm template "$deployment" "$FLINK_DEPLOYMENT_REPO" \
      --set image.imageName="$DEPLOY_IMAGE_NAME" \
      --set image.imageTag="$DEPLOY_IMAGE_TAG"
  )

  if [[ -z "$@" ]]; then
    eval "$helm_cmd" -f "$values_path" --debug >>"$SCRIPT_DIRECTORY"/template.yaml
  else
    local set_flag=$(_parse_args_flag "$@")
    eval "$helm_cmd" "$set_flag" -f "$values_path" --debug >>"$SCRIPT_DIRECTORY"/template.yaml
  fi

  echo "$(cat "$SCRIPT_DIRECTORY"/template.yaml)"
  #  rm -f "$SCRIPT_DIRECTORY"/template.yaml
}

cmd_clean() {
  local deployment=$1
  shift || error "Missing deployment. $USAGE"
  if [[ -z "$deployment" ]]; then
    error "Deployment is empty."
  fi

  if _check_deploy_exist "$deployment"; then
    echo "$deployment is exits."
    kubectl delete flinkdeployment/"$deployment"
    helm get values "$deployment" --output=json | jq '.persistentVolumeClaim.name' | xargs kubectl delete pvc || true
    helm uninstall "$deployment"
  fi
}

cmd_deploy() {
  local deployment=$1
  shift || error "Missing deployment. $USAGE"
  if [[ -z "$deployment" ]]; then
    error "Deployment is empty."
  fi

  local values_path=$1
  shift || error "Missing helm values.yaml. $USAGE"
  [ -f "$values_path" ] || error "Values path does not exist."

  if _check_deploy_exist "$deployment"; then
    echo "$deployment is already deployed."
  fi

  local helm_cmd=$(
    echo helm upgrade --install "$deployment" "$FLINK_DEPLOYMENT_REPO" \
      --set image.imageName="$DEPLOY_IMAGE_NAME" \
      --set image.imageTag="$DEPLOY_IMAGE_TAG"
  )

  echo "Deploying $deployment ..."
  if [[ -z "$@" ]]; then
    eval "$helm_cmd" -f "$values_path" --debug
  else
    local set_flag=$(_parse_args_flag "$@")
    eval "$helm_cmd" "$set_flag" -f "$values_path" --debug
  fi

  sleep 3
  while true; do
    echo "Wait for deployment completed ..."
    if _check_deploy_exist "$deployment"; then
      kubectl describe deployment "$deployment"
      break
    fi
  done

  echo "Wait for deployment ready ..."
  kubectl rollout status deployment "$deployment" &
  wait_until_done $! 1

  # run forever
  tail -f /dev/null
}

main() {
  local cmd=$1
  shift || error "Missing command, $USAGE."
  cmds=("$cmd")

  # multiple command
  local yes=0
  while true; do
    cmd=$1
    echo "$cmd"
    yes=0
    for defined in deploy clean tempate; do
      if [[ "$cmd" == "$defined" ]]; then
        yes=1
        break
      fi
    done
    [ "$yes" == "0" ] && break
    cmds+=("$cmd")
    shift
  done

  for cmd in "${cmds[@]}"; do
    cmd_"$cmd" "$@"
  done
}

main "$@"
