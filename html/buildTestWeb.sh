#!/usr/bin/env bash

../gradlew :html:build
wait
if [[ -d ../release/webapp/ ]]; then
  python3 -m http.server 8100 -d ../release/webapp/
fi
