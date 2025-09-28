FROM gradle:8.14.0-jdk-21-and-24-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build --no-daemon

FROM eclipse-temurin:25-jre-alpine
EXPOSE 8093
WORKDIR /app
COPY --from=build /app/build/libs/playnite-simple-sync-server-*.jar ./app.jar
ENV SYNC_SERVER_PORT=8093
ENV SYNC_SERVER_LOG_DIR=./logs/
ENV SYNC_SERVER_DB_HOST=localhost
ENV SYNC_SERVER_DB_PORT=49010
ENV SYNC_SERVER_DB_NAME=playnite
ENV SYNC_SERVER_DB_USER=playnite
ENV SYNC_SERVER_DB_PASSWORD=playnite
ENV SYNC_SERVER_MAX_FILE_SIZE=100MB
ENV SYNC_SERVER_METADATA_FOLDER=./metadata
CMD ["java", "-jar", "app.jar"]