FROM maven:3.8.6-openjdk-18-slim AS builder
COPY . .
RUN mvn package

FROM openjdk:18.0.2.1-slim-buster
WORKDIR /app
COPY --from=first /target/*.jar /app/
RUN chmod 755 entry.sh
ENTRYPOINT ["./entry.sh"]