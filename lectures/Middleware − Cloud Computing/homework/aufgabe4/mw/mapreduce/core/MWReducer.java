package mw.mapreduce.core;

import java.io.IOException;
import java.util.HashMap;

import mw.mapreduce.core.MWContext.MWReduceContext;
import mw.mapreduce.util.MWPair;

public class MWReducer implements Runnable {

    protected MWReduceContext context;

    public MWReducer() {
    }

    public MWReducer(final MWReduceContext context) {
        this.context = context;
    }

    public void run() {
        // int profiles = 0;
        try {
            MWPair<String, Iterable<String>> pair = context.getReader().read();
            while(pair != null){
                // profiles++;
                //System.out.println(context.reducerID + " PAIR: " + pair.toString());
                reduce(pair.getKey(), pair.getValue(), context);
                pair = context.getReader().read();
            }
            // System.out.println("REDUCER PROFILES: " + profiles);         // Ausgabe, um festzustellen, ob man alle Profile extrahiert hat
            context.outputComplete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void setContext(MWReduceContext context) {   // TODO Sinn dieser Methode gegenwärtig noch unklar
        this.context = context;
    }

    protected void reduce(String key, Iterable<String> values, MWContext<?> context) throws Exception {
        // Parameter „context“ soll identisch mit „this.context“ sein
        for (String value : values) {
            context.write(key, value);
        }
    }
}
