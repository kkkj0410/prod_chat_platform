#FROM openjdk:17-jdk-slim
FROM eclipse-temurin:17-jdk-jammy

# firebase json 파일을 넣을 수 있게 미리 폴더 생성
WORKDIR /app
RUN mkdir -p resources/firebase

COPY build/libs/app-0.0.1-SNAPSHOT.jar app.jar
#ENTRYPOINT ["java", "-jar", "/app.jar"]

ENV JAVA_OPTS="-Xms1g -Xmx1g"

# JVM 시간 설정 - 서울 기준 (스케쥴러 작동을 위함)
ENV TZ=Asia/Seoul
#ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Duser.timezone=${TZ}", "-jar", "/app.jar"]
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -Duser.timezone=${TZ} -jar /app.jar"]