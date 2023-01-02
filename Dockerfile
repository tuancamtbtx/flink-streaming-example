ARG FLINK_VERSION=1.15.2
ARG SCALA_VERSION=2.12

FROM docker.io/gradle:jdk11-focal as builder

ENV GRADLE_USER_HOME $HOME/gradle

WORKDIR /example/
COPY ./settings.gradle ./build.gradle ./gradle.properties ./

RUN  gradle --no-daemon jar -x test  || true

RUN gradle --no-daemon jar -x test || true

# bundle deps into single files
RUN  gradle --no-daemon shadowJar

# now build our codes
COPY . .
RUN gradle --no-daemon jar -x test

FROM flink:${FLINK_VERSION}

ARG FLINK_VERSION
ARG SCALA_VERSION

# enable flink gcs
RUN mkdir -p $FLINK_HOME/plugins/gs-fs-hadoop \
    &&  cp ./opt/flink-gs-fs-hadoop-${FLINK_VERSION}.jar $FLINK_HOME/plugins/gs-fs-hadoop/

# cache libs
RUN set -ex; download_lib="wget -nv --content-disposition"; cd $FLINK_HOME/lib; FLINK_SHORT_VERSION=$(echo "$FLINK_VERSION" | cut -c 3-); \
  $download_lib https://repo1.maven.org/maven2/org/apache/flink/flink-connector-kafka/${FLINK_VERSION}/flink-connector-kafka-${FLINK_VERSION}.jar; \
  $download_lib https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/2.8.1/kafka-clients-2.8.1.jar;

RUN mkdir -p $FLINK_HOME/config

COPY --chown=flink:flink --from=builder /example/config/ $FLINK_HOME/config/
COPY --chown=flink:flink --from=builder /example/config/flink-conf.yaml $FLINK_HOME/conf/flink-conf.yaml

COPY --chown=flink:flink --from=builder /example/flink-streaming-k8s-example/build/libs/*.jar $FLINK_HOME/lib/

EXPOSE 6122 8081 6123