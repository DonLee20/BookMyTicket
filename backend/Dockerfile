# syntax=docker/dockerfile:1
# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package && cp target/app.jar /build/app.jar

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system travel \
    && useradd --system --gid travel --home /app --shell /usr/sbin/nologin travel

WORKDIR /app
COPY --from=build /build/app.jar /app/app.jar
RUN chown -R travel:travel /app
USER travel

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
