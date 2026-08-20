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

echo STARTS WITH
url="${endpoint}/api/starts/$1"
curl "$url"
echo

echo INCLUDES
url="${endpoint}/api/contains/$1"
curl "$url"
echo

echo MATCHES
url="${endpoint}/api/matches/$1"
curl "$url"
echo