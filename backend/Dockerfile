# Builds the Quarkus app from source; tests run in CI, not in the image build.
FROM eclipse-temurin:17-jdk AS build
# Without unzip, mvnw downloads the .tar.gz and fails the checksum pinned for the .zip
RUN apt-get update && apt-get install -y --no-install-recommends unzip && rm -rf /var/lib/apt/lists/*
WORKDIR /src
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests

FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.24
ENV LANGUAGE='en_US:en'
COPY --from=build --chown=185 /src/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /src/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /src/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /src/target/quarkus-app/quarkus/ /deployments/quarkus/
EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
