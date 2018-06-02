DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

for cls in $@; do
  EVALPATH="evaluation/$cls"
  mkdir -p $EVALPATH

  pushd $EVALPATH

  hyperfine --warmup 3 -i --export-json pref.json \
            "java -ea -cp '$DIR/test/abcd/classes/' $cls"\
            "'$DIR/record.sh' $cls history.log"\
            "'$DIR/replay-log.sh' $cls history.log"

  until "$DIR/record.sh" $cls history.log 2>&1 | grep java.lang.AssertionError; do
      echo "$cls: failed at producing assertion error"
  done

  start=$(date +%s.%N)
  python "$DIR/../linedd/linedd" --stats stats.csv --expect 0 history.log dd.log "'$DIR/replay-log.sh' $cls" | tee linedd.msg
  end=$(date +%s.%N)

  popd

done
