#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
./compilar.sh
java -Dfile.encoding=UTF-8 -cp bin juego.Principal
