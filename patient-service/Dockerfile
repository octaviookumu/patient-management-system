FROM maven:3.9-eclipse-temurin-21 AS build
LABEL authors="octavian"

# A container is going to have a whole bunch of files of it that it needs to start the container and run files
# So specifying a working directory keeps our own custom code in a single location we know
WORKDIR /app

# Copy it into our app
COPY pom.xml .

COPY patient-service/pom.xml patient-service/pom.xml

# Cache the dependencies unless a change has been detected
RUN mvn -f patient-service/pom.xml dependency:go-offline -B

# We need this before we can compile everything inside the image
COPY patient-service/src patient-service/src

# Package everything up into a single jar
# Create a new package from our spring boot app
# This will create a single .jar file that has all our stuff in it
# means “run Maven’s clean package goals using patient-service/pom.xml.”
# That matters because the Docker build context is the patient-management-system root,
# while the service’s POM is in the patient-service subdirectory.
# Without -f, Maven looks for pom.xml in its current directory (/app) and may not find the service project.
RUN mvn -f patient-service/pom.xml clean package -DskipTests

# Describes instructions on how to run the build code
FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

# Copy patient-service snapshot jar (created in build step) into the runner step at ./app.jar
COPY --from=build /app/patient-service/target/patient-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4000

# A command for the run step to use to run our code
ENTRYPOINT ["java", "-jar", "app.jar"]

# A Dockerfile tells Docker how to build an image.
# Your Compose file tells Docker how to run containers