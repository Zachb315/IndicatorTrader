FROM amazoncorretto:17
EXPOSE 8080
ADD target/Trader-Docker.jar
ENTRYPOINT ["java", "-jar", "/Trader-Docker.jar"]