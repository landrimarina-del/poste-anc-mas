FROM flowable/flowable-ui:6.8.0

# L'entrypoint usa: -cp /app:/app/WEB-INF/classes:/app/WEB-INF/lib/*
# Basta copiare il driver in /app/WEB-INF/lib/ per averlo sul classpath.
# Nota: ADD <URL> crea file con permessi 0600 (solo root), per questo
# usiamo wget + chmod 644 in un unico RUN per rispettare i permessi degli altri JAR.
USER root

RUN wget -q -O /app/WEB-INF/lib/mariadb-java-client-3.3.3.jar \
      https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.3.3/mariadb-java-client-3.3.3.jar \
    && chmod 644 /app/WEB-INF/lib/mariadb-java-client-3.3.3.jar

USER flowable