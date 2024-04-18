# FROM openjdk:17-oracle
FROM amazoncorretto:17.0.7-alpine
ARG version=17.0.10
WORKDIR /app
COPY build/libs/prmo-0.0.1-SNAPSHOT.jar app.jar

RUN apk add --no-cache msttcorefonts-installer fontconfig
RUN update-ms-fonts

ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8080