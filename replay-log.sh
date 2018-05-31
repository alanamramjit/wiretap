#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -e
java -ea -javaagent:"$DIR/build/wiretap.jar" \
  -Dwiretap.recorder=ABCDReplayer -Dwiretap.replayfile=$2 -cp \
  "$DIR/test/abcd/classes/" $1 2>&1 | tee abcdd.msg
set +e

grep java.lang.AssertionError abcdd.msg > /dev/null
