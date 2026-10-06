FROM maven:3.9-eclipse-temurin-21 AS build
LABEL authors="octavian"

WORKDIR /app

COPY pom.xml .

COPY auth-service/pom.xml auth-service/pom.xml

# Cache the dependencies unless a change has been detected
RUN mvn -f auth-service/pom.xml dependency:go-offline -B

COPY auth-service/src auth-service/src

# Without -f, Maven looks for pom.xml in its current directory (/app) and may not find the service project.
RUN mvn -f auth-service/pom.xml clean package -DskipTests

# Describes instructions on how to run the build code
FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

# Copy auth-service snapshot jar (created in build step) into the runner step at ./app.jar
COPY --from=build /app/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4005

# A command for the run step to use to run our code
ENTRYPOINT ["java", "-jar", "app.jar"]