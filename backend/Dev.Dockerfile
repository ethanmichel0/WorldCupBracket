FROM maven:3.8.6-openjdk-18-slim
WORKDIR /app
COPY ./.mvn ./mvn
COPY ./mvnw ./
COPY ./pomDev.xml ./
# Note that src is mounted as a volume in docker ignore.
ENTRYPOINT mvn spring-boot:run -f pomDev.xml