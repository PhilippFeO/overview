./compile.sh

rm -r tmpfiles/*
rm -r outfiles/*
java -classpath "bin" mw.mapreduce.MWMapReduce friendcount friends.list tmp out 4 4