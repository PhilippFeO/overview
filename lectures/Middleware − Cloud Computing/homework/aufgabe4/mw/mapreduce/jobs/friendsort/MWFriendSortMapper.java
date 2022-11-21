package mw.mapreduce.jobs.friendsort;

import mw.mapreduce.core.MWContext;
import mw.mapreduce.core.MWMapper;
import mw.mapreduce.core.MWContext.MWMapContext;

public class MWFriendSortMapper extends MWMapper{

	//private MWMapContext context;

	public MWFriendSortMapper(MWMapContext context){
		super.context = context;
	}

	// Reader in „MWMapper.run()“ liest „<ID>\t<#Freunde>“ ein => key = <ID>, value = <#Freunde>
	// Ausgabe soll als „<#Freunde>\t<ID>“ erfolgen => „key“ und „value“ vertauschen
	protected void map(String key, String value, MWContext<?> context) throws Exception {
        // Parameter „context“ soll identisch mit „this.context“ sein
		//System.out.println(this.getClass().getName() + ": " + key + " - " + value + "  (wird vertauscht an map() übergeben)");

        context.write(value, key);	// Vertauscht
    }
}
