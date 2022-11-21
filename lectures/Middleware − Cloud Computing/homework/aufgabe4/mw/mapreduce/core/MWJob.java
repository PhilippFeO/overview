package mw.mapreduce.core;

import mw.mapreduce.reader.MWReader;
import mw.mapreduce.reader.MWKeyValueReader;
import mw.mapreduce.core.MWContext.MWMapContext;
import mw.mapreduce.core.MWContext.MWReduceContext;


import java.util.Comparator;

public class MWJob {
    class StringComparator implements Comparator<String> {
        public int compare(String obj1, String obj2) {
            if (obj1 == obj2) {
                return 0;
            }
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            return obj1.compareTo(obj2);
        }
    }

    final StringComparator strcomparator = new StringComparator();

    public MWMapper createMapper(MWMapContext context){
        return new MWMapper(context);
    }

    public MWReducer createReducer(MWReduceContext context){
        return new MWReducer(context);
    }

    public MWReader createInputReader(String path, long start, long length) throws Exception{
        return new MWKeyValueReader(path, start, length);
    }

    public java.util.Comparator<String> getComparator(){
        return strcomparator;
    }
}
