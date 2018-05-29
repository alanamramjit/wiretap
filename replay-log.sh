#!/usr/bin/env bash

java -ea -javaagent:../wiretap/build/wiretap.jar \
  -Dwiretap.recorder=ABCDReplayer -Dwiretap.replayfile=$2 -cp \
  test/abcd/classes/ $1 2>&1 | grep java.lang.AssertionError