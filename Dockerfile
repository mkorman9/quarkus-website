FROM azul/zulu-openjdk-debian:21-jre-headless

COPY --chown=nobody:nogroup target/quarkus-app/lib/ /deployment/lib/
COPY --chown=nobody:nogroup target/quarkus-app/*.jar /deployment/
COPY --chown=nobody:nogroup target/quarkus-app/app/ /deployment/app/
COPY --chown=nobody:nogroup target/quarkus-app/quarkus/ /deployment/quarkus/

USER nobody
WORKDIR /

EXPOSE 8080
EXPOSE 8443

CMD [ "java", "-jar", "/deployment/quarkus-run.jar" ]
