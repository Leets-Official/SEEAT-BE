# 1. JDK 이미지를 베이스로 설정
FROM eclipse-temurin:17-jdk

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. jar 파일 복사
ARG JAR_FILE=build/libs/server-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 4. 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
