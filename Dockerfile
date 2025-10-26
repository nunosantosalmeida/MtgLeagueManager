FROM eclipse-temurin:17.0.16_8-jdk

WORKDIR /app

COPY entrypoint.sh ./

COPY target/quarkus-app/ ./

EXPOSE 8080

CMD ["./entrypoint.sh"]
