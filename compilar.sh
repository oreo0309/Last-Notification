#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
if ! command -v javac >/dev/null 2>&1; then
    echo "No se encontró javac. Instala un JDK 17 o superior."
    exit 1
fi
rm -rf bin
mkdir -p bin
javac --release 17 -encoding UTF-8 -d bin -sourcepath src src/juego/Principal.java
javac --release 17 -encoding UTF-8 -d bin -cp bin -sourcepath "test:src" test/pruebas/EjecutarPruebas.java
echo "Compilación exitosa."
