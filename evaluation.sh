DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

for cls in $@; do
  EVALPATH="evaluation/$cls"
  mkdir -p $EVALPATH

  pushd $EVALPATH

  hyperfine -i --export-json pref.json \
            "java -ea -cp '$DIR/test/abcd/classes/' $cls"\
            "'$DIR/record.sh' $cls history.log"\
            "'$DIR/replay-log.sh' $cls history.log"

  until "$DIR/record.sh" $cls history.log 2>&1 | grep java.lang.AssertionError; do
      echo "$cls: failed at producing assertion error"
  done

  time python "$DIR/../linedd/linedd" --expect 0 history.log dd.log "'$DIR/replay-log.sh' $cls" | tee linedd.msg

  popd

done
