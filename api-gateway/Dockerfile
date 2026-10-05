FROM maven:3.9-eclipse-temurin-21 AS build
LABEL authors="octavian"

WORKDIR /app

# Copy it into our app
COPY pom.xml .

COPY api-gateway/pom.xml api-gateway/pom.xml

# Cache the dependencies unless a change has been detected
RUN mvn -f api-gateway/pom.xml dependency:go-offline -B

# We need this before we can compile everything inside the image
COPY api-gateway/src api-gateway/src

RUN mvn -f api-gateway/pom.xml clean package -DskipTests

# Describes instructions on how to run the build code
FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

# Copy api-gateway snapshot jar (created in build step) into the runner step at ./app.jar
COPY --from=build /app/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4004

ENTRYPOINT ["java", "-jar", "app.jar"]