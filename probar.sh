#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
./compilar.sh
java -Djava.awt.headless=true -Dfile.encoding=UTF-8 -cp bin pruebas.EjecutarPruebas
