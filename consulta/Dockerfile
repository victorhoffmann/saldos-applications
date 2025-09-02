FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/consulta-app.jar app.jar
EXPOSE 8080
# ENTRYPOINT ["java", \
#   "-Xms512m", "-Xmx1024m", \
#   "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", \
#   "-XX:MaxMetaspaceSize=256m", \
#   "-jar", "app.jar"]
ENTRYPOINT ["java", "-jar", "app.jar"]