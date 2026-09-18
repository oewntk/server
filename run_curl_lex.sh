#!/bin/bash

# Copyright (c) 2021-2026. Bernard Bou.
# expects arg like "row,n-1"
# expects arg like "row,v"

set -Eeo pipefail
on_err() {
  local exit_code=$?
  local line_no=${BASH_LINENO[0]}
  echo "Error on line $line_no (exit code: $exit_code)."
}
trap on_err ERR

endpoint="http://localhost:8080"
target="$1"
target=${target//%/%25}
target=${target//:/%3A}
target=${target// /%20}

echo LEX
url="${endpoint}/api/lex/$target"
curl "$url"
echo
