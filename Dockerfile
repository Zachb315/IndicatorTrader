FROM amazoncorretto:17
EXPOSE 8080
ADD target/trader-docker.jar trader-docker.jar
ENTRYPOINT ["java", "-jar", "/trader-docker.jar"]