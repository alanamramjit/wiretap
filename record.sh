#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

java -ea -javaagent:"$DIR/build/wiretap.jar" -Dwiretap.recorder=ABCDRecorder -cp "$DIR/test/abcd/classes/" $1
cp _wiretap/history.log $2
