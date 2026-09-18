#!/bin/bash

# Copyright (c) 2021-2026. Bernard Bou.

set -Eeo pipefail
on_err() {
  local exit_code=$?
  local line_no=${BASH_LINENO[0]}
  echo "Error on line $line_no (exit code: $exit_code)."
}
trap on_err ERR

jar=oewn-server-3.0.2-uber.jar
if [ ! -e "${jar}" ]; then
  if [ ! -e "target/${jar}" ]; then
    echo "Non existing uber jar" >&2
    exit 1
    fi
  ln -s "target/${jar}"
  fi
if [ ! -e "${jar}" ]; then
  echo "Non existing uber jar" >&2
  exit 2
  fi

# looks for yaml subdirectory in current working directory
# use -P:model.path=<path>/yaml otherwise
java -jar "${jar}" org.oewntk.json.server.MainKt -P:model.path=yaml_model -P:model.type=yaml "$@"
#java -ea -cp "${jar}" org.oewntk.json.server.MainKt  -P:model.path=yaml_model -P:model.type=yaml "$@"
