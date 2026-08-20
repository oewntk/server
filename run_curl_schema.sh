#!/bin/bash

# Copyright (c) 2021-2026. Bernard Bou.

set -Eeo pipefail
on_err() {
  local exit_code=$?
  local line_no=${BASH_LINENO[0]}
  echo "Error on line $line_no (exit code: $exit_code)."
}
trap on_err ERR

endpoint="http://localhost:8080"

types="$1"
if [ -z "${types}" ]; then
  types="oewn data model schema-oewn schema-data schema-model defs-oewn defs-data defs-model"
fi
for t in ${types}; do
  echo "$t" >&2
  url="${endpoint}/api/schema/${t}"
  curl "$url"
  echo
done