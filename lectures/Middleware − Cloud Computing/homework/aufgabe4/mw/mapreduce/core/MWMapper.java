package mw.mapreduce.core;


import java.util.Comparator;

import mw.mapreduce.core.MWContext.MWMapContext;
import mw.mapreduce.util.MWPair;

public class MWMapper implements Runnable {

    protected MWMapContext context;

    public MWMapper(){}

    public MWMapper(final MWMapContext context){
        this.context = context;
    }

    public void run(){
        // Aufgabe der run()-Methode ist es jeweils, die dem Worker per Kontext
        // zugewiesenen Daten unter Einbeziehung der map()- bzw. der reduce()-Methode zu
        // verarbeiten.
            // Wasl. Daten mit Reader lesen und dann map(key, value, context) aufrufen

        try {
            MWPair<String, String> pair = context.getReader().read();
            while(pair != null) {
                map(pair.getKey(), pair.getValue(), context);
                pair = context.getReader().read();
            }
            context.outputComplete();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setContext(MWMapContext context){   // TODO Sinn dieser Methode gegenwärtig noch unklar
        this.context = context;
    }

    protected void map(String key, String value, MWContext<?> context) throws Exception {
        // Parameter „context“ soll identisch mit „this.context“ sein
        //System.out.println(this.getClass().getName() + ": " + key + " - " + value);
        context.write(key, value);
    }

}