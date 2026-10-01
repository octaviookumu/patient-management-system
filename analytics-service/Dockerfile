FROM maven:3.9-eclipse-temurin-21 AS build
LABEL authors="octavian"

WORKDIR /app

COPY pom.xml .

COPY analytics-service/pom.xml analytics-service/pom.xml

# Cache the dependencies unless a change has been detected
RUN mvn -f analytics-service/pom.xml dependency:go-offline -B

COPY analytics-service/src analytics-service/src

RUN mvn -f analytics-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

# Copy analytics-service snapshot jar (created in build step) into the runner step at ./app.jar
COPY --from=build /app/analytics-service/target/analytics-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4002

# A command for the run step to use to run our code
ENTRYPOINT ["java", "-jar", "app.jar"]
