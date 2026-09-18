#!/bin/bash

# Copyright (c) 2021-2026. Bernard Bou.

set -Eeo pipefail
on_err() {
  local exit_code=$?
  local line_no=${BASH_LINENO[0]}
  echo "Error on line $line_no (exit code: $exit_code)."
}
trap on_err ERR

./run_curl_lemma.sh "row"
./run_curl_lex.sh "row,n-1"
./run_curl_sense.sh "row%1:14:00::"
./run_curl_synset.sh "08448447-n"