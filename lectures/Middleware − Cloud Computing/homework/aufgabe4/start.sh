rm -r tmpfiles/*
rm -r outfiles/*
java -classpath "bin" mw.mapreduce.MWMapReduce app num_friends_small.list tmp out