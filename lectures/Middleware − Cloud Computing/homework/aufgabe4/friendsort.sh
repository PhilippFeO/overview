./compile.sh

rm -r tmpfiles/*
rm -r outfiles/*
java -classpath "bin" mw.mapreduce.MWMapReduce friendsort num_friends.list tmp out 4 4