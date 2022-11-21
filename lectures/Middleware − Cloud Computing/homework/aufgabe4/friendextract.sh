./compile.sh

rm -r tmpfiles/*
rm -r outfiles/*
java -classpath "bin" mw.mapreduce.MWMapReduce friendextract data-small.dump tmp out 8 4