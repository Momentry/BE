# 1단계: 빌드 결과물 추출 (레이어 분리)
FROM eclipse-temurin:21-jre-alpine AS extract
WORKDIR /app
# 위 yml에서 생성한 bootJar 파일을 복사
COPY build/libs/*.jar app.jar
# 스프링 부트 4의 레이어 추출 기능 사용
RUN java -Djarmode=layertools -jar app.jar extract

# 2단계: 실제 실행 이미지 생성
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 변경이 거의 없는 의존성을 먼저 복사 (GHA 캐시가 여기서 터짐!)
COPY --from=extract /app/dependencies/ ./
COPY --from=extract /app/spring-boot-loader/ ./
COPY --from=extract /app/snapshot-dependencies/ ./
# 자주 바뀌는 내 코드만 마지막에 복사
COPY --from=extract /app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

EXPOSE 8080