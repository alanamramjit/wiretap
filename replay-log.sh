#!/usr/bin/env bash

set -e
java -ea -javaagent:../wiretap/build/wiretap.jar \
  -Dwiretap.recorder=ABCDReplayer -Dwiretap.replayfile=$2 -cp \
  test/abcd/classes/ $1 2>&1 | tee abcdd.msg 
set +e

grep java.lang.AssertionError abcdd.msg > /dev/null
