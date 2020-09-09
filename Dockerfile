FROM openjdk:8-jre
WORKDIR /root/
COPY build/libs/secure-pipeline-demo-1.0.jar .
EXPOSE 8080
CMD ["java","-jar","secure-pipeline-demo-1.0.jar"]