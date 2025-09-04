FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=build /app/target/ingestao-app.jar app.jar
EXPOSE 8081

ENTRYPOINT ["java", \
  "-Xms2048m", "-Xmx4096m", \
  "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", \
  "-XX:InitiatingHeapOccupancyPercent=45", \
  "-XX:MaxMetaspaceSize=512m", \
  "-XX:+ParallelRefProcEnabled", \
  "-jar", "app.jar"]