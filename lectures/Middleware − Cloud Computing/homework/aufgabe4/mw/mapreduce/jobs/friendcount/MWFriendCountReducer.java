package mw.mapreduce.jobs.friendcount;

import java.io.IOException;
import java.util.Iterator;

import mw.mapreduce.core.MWReducer;
import mw.mapreduce.core.MWContext.MWReduceContext;
import mw.mapreduce.core.MWContext;

public class MWFriendCountReducer extends MWReducer {

	public MWFriendCountReducer(MWReduceContext context) {
		super.context = context;
	}

	protected void reduce(String key, Iterable<String> values, MWContext<?> context) throws Exception {
        String name="";
		int numberOfFriends = 0;
		for(String value : values) {
			if(value.startsWith("|")){
				name = value.substring(1);
				continue;
			}
			numberOfFriends++;
		}

		context.write(name, numberOfFriends+"");
    }
}
