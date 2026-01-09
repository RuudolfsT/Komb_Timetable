# -------- Build stage --------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (cached layer if pom.xml doesn't change)
# Using dependency:resolve instead of go-offline for better reliability
RUN mvn dependency:resolve

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]