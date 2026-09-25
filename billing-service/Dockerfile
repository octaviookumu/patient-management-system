FROM maven:3.9-eclipse-temurin-21 AS build
LABEL authors="octavian"

WORKDIR /app

COPY pom.xml .

COPY billing-service/pom.xml billing-service/pom.xml

RUN mvn -f billing-service/pom.xml dependency:go-offline -B

COPY billing-service/src billing-service/src

RUN mvn -f billing-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

COPY --from=build /app/billing-service/target/billing-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4001

# port for grpc server
EXPOSE 9001

ENTRYPOINT ["java", "-jar", "app.jar"]
