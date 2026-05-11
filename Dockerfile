FROM amazoncorretto:21-alpine AS builder
WORKDIR /app
COPY . .

RUN chmod +x ./mvnw

RUN ./mvnw clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]