#!/usr/bin/env bash

java -ea -javaagent:../wiretap/build/wiretap.jar -Dwiretap.recorder=ABCDRecorder -cp test/abcd/classes/ $1
cp _wiretap/history.log $2
