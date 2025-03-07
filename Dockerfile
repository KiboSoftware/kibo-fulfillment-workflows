# Stage 1: Build and Test with OpenJDK 8
FROM maven:3.8.4-openjdk-11 AS build
WORKDIR /app

COPY . .

ARG BUILD_VER
ENV BUILD_VER=$BUILD_VER
ARG skiptest=false
ARG MAVEN_OPTS="-DskipTests=$skiptest -Dmaven.test.failure.ignore=true"
ENV MAVEN_OPTS=$MAVEN_OPTS
ARG MAVEN_PUBLISH_KEY

RUN sed -i -r "s/MAVENPUBLISHKEY/${MAVEN_PUBLISH_KEY}/g" maven_settings.xml \
 && chmod 755 /app/set-deploy-version.sh \
 && bash ./set-deploy-version.sh \
 && mkdir -p /app/target/jacoco-aggregate \
 && echo "MAVEN_OPTS=$MAVEN_OPTS" \
 && mvn -B -e -s maven_settings.xml clean package

# Stage 2: Scan snd Deploy with OpenJDK 17
FROM maven:3.8.4-openjdk-17 AS sonar
WORKDIR /app

COPY --from=build /app /app

ARG SONAR_SCAN=enabled
ENV SONAR_SCAN=$SONAR_SCAN

RUN echo "MAVEN_OPTS=$MAVEN_OPTS; SONAR_SCAN=$SONAR_SCAN" \
 && chmod 755 /app/sonarscanner/sonarnet.sh \
 && /app/sonarscanner/sonarnet.sh mvn validate \
 && mvn -B -e -s maven_settings.xml deploy -fn -P nexus-deploy
