package mw.mapreduce.jobs.friendextract;

import java.io.IOException;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import mw.mapreduce.core.MWReducer;
import mw.mapreduce.core.MWContext.MWReduceContext;
import mw.mapreduce.core.MWContext;

public class MWFriendExtractReducer extends MWReducer {

	public MWFriendExtractReducer(MWReduceContext context) {
		super.context = context;
	}

	protected void reduce(String key, Iterable<String> values, MWContext<?> context) throws Exception {
		Set<String> seenValues = new HashSet<String>();
		for (String value : values) {
			if(!seenValues.contains(value)) {
				seenValues.add(value);
            	context.write(key, value);
			}
        }
	}
}
