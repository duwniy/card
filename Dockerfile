FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Копируем Maven wrapper и pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Скачиваем зависимости (кешируется)
RUN ./mvnw dependency:go-offline

# Копируем исходники
COPY src ./src

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем jar из build stage
COPY --from=build /app/target/*.jar app.jar

# Создаем пользователя без прав root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Открываем порт
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Запуск приложения
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseContainerSupport", \
    "-jar", \
    "app.jar"]