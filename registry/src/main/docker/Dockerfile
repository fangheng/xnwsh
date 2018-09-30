FROM openjdk:8-jre-alpine
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=dev,native \
    GIT_URI=https://github.com/jhipster/jhipster-registry/ \
    GIT_SEARCH_PATHS=central-config

ADD *.war /app.war
EXPOSE 8761
RUN mkdir /target && \
    chmod g+rwx /target
CMD java \
        ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom \
        -jar /app.war \
        --spring.cloud.config.server.git.uri=${GIT_URI} \
        --spring.cloud.config.server.git.search-paths=${GIT_SEARCH_PATHS}

