FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# --- Cambio de base: Alpine -> Ubuntu (jammy) ---
# La imagen anterior era eclipse-temurin:21-jre-alpine (para Selenium +
# chromium por apk). Playwright NO soporta Alpine/musl: los binarios de
# Chromium que descarga son para glibc, no corren ahí -- es una limitación
# conocida de Playwright, no un paquete que falte. Por eso esta imagen pasa
# a Ubuntu, que es lo que Playwright soporta oficialmente.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# python3/pip: para correr scripts/descargar_adjuntos.py.
# xvfb: Chromium sigue corriendo con interfaz gráfica real (no headless) --
# el sitio bloquea el modo headless -- así que necesita un display, aunque
# sea virtual. A diferencia de la versión Selenium (un solo Xvfb global
# para toda la vida del proceso Java), acá cada descarga se envuelve con
# "xvfb-run -a" por separado (ver application-linux.yml), así que no hace
# falta levantar Xvfb a mano en este Dockerfile.
# tini: sigue como PID 1 real para recolectar (reap) procesos zombie de
# xvfb-run/Chromium -- sin esto se acumulan hasta agotar los procesos del
# sistema operativo (esto fue lo que tumbó el backend antes, con Selenium).
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \
    xvfb \
    tini \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar
COPY scripts ./scripts

# "--with-deps" baja el binario de Chromium Y las librerías de sistema que
# necesita (libnss, fuentes, etc.) en un solo paso -- por eso no se listan
# a mano como se hacía con "apk add chromium ...".
RUN pip3 install --no-cache-dir -r scripts/requirements.txt \
    && python3 -m playwright install --with-deps chromium

# Esta imagen es siempre Linux headless -> activa el perfil que antepone
# xvfb-run a cada invocación del script Python (ver application-linux.yml).
ENV SPRING_PROFILES_ACTIVE=linux

EXPOSE 8082
ENTRYPOINT ["/usr/bin/tini", "--", "java", "-jar", "app.jar"]
