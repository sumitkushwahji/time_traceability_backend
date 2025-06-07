FROM openjdk:17-jdk

WORKDIR /app

COPY target/tracealibility-0.0.1-SNAPSHOT.jar /app/tracealibility.jar
# COPY target/spring-boot-security-postgresql-0.0.1-SNAPSHOT.jar /app/auth-service.jar
# COPY start.sh /app/start.sh

# EXPOSE 7001
EXPOSE 7002


# RUN chmod +x /app/start.sh

# CMD ["/bin/bash", "/app/start.sh"]
CMD ["java", "-jar", "tracealibility.jar"]

