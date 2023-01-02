FROM dtzar/helm-kubectl:3.10

ARG DEPLOY_IMAGE_NAME
ARG DEPLOY_IMAGE_TAG

ENV DEPLOY_IMAGE_NAME="${DEPLOY_IMAGE_NAME}"
ENV DEPLOY_IMAGE_TAG="${DEPLOY_IMAGE_TAG}"

RUN mkdir -p /deployment
COPY ./helm /deployment/helm
COPY ./.ci /deployment/.ci

COPY ./tools/bin/deploy.sh /deploy.sh

WORKDIR /deployment

ENTRYPOINT ["/deploy.sh"]
