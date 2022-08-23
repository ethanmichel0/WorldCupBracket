FROM maven:3.8.6-openjdk-18-slim
WORKDIR /app
COPY . .

RUN mvn package
RUN chmod 755 ./entry.sh
ENTRYPOINT ["./entry.sh"]