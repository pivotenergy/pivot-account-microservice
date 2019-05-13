FROM openjdk:8-jre-alpine
ADD target/micro-service.jar /app.jar
RUN touch /app.jar
ENTRYPOINT ["java","-XX:+UseG1GC","-Xmx64m", "-Xss256k", "-Djava.security.egd=file:/dev/urandom","-jar","/app.jar"]