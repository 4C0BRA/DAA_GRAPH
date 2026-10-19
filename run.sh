#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p out
find src -name '*.java' | sort > sources.txt
javac -encoding UTF-8 -d out @sources.txt
case "${1:-demo}" in
  demo) java -cp out Main ;;
  test) java -cp out Tests ;;
  benchmark) java -cp out Experiments ;;
  *) echo 'Usage: sh run.sh [demo|test|benchmark]' >&2; exit 1 ;;
esac
