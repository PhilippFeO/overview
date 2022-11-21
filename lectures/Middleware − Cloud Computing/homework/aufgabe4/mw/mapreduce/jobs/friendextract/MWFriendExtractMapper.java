package mw.mapreduce.jobs.friendextract;

import mw.mapreduce.core.MWContext;
import mw.mapreduce.core.MWMapper;
import mw.mapreduce.core.MWContext.MWMapContext;

public class MWFriendExtractMapper extends MWMapper{

	//private MWMapContext context;
	String lastKey = null;
	String lastValue = null;

	public MWFriendExtractMapper(MWMapContext context){
		super.context = context;
	}

	protected void map(String key, String value, MWContext<?> context) throws Exception {
		if((key.equals(lastKey) && value.equals(lastValue)) == false) {
			context.write(key, value);
			context.write(value, key);
		}
		lastKey = key;
		lastValue = value;
    }
}
