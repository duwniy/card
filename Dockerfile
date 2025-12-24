# Этап 1: Сборка
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
# Копируем pom.xml и скачиваем зависимости (кешируем этот слой)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходники и собираем проект
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Копируем только готовый JAR из первого этапа
COPY --from=build /app/target/*.jar app.jar

# Настройки для оптимизации Java в контейнере
ENTRYPOINT ["java", "-jar", "app.jar"]