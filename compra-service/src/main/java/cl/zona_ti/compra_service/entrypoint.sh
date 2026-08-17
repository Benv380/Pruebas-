#!/bin/sh
set -e

# Levanta un display virtual (nadie lo va a "ver", pero Chrome necesita creer
# que hay una pantalla real para no comportarse como headless, que es lo que
# mercadopublico.cl termina detectando y bloqueando).
Xvfb :99 -screen 0 1920x1080x24 -nolisten tcp &
export DISPLAY=:99

# Pequeña espera para que Xvfb termine de levantar antes de que Chrome intente usarlo.
sleep 1

exec java -jar app.jar