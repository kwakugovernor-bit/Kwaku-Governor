# ---- build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---- run ----
FROM eclipse-temurin:17-jre
RUN useradd --system --create-home hub && mkdir -p /data/uploads && chown -R hub /data
USER hub
WORKDIR /app
COPY --from=build /src/target/african-heritage-hub-1.0.0.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
VOLUME /data
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD curl -fs http://localhost:8080/login >/dev/null || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
