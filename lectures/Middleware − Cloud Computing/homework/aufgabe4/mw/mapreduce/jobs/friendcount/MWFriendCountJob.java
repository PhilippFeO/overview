package mw.mapreduce.jobs.friendcount;

import mw.mapreduce.core.MWJob;
import mw.mapreduce.core.MWMapper;
import mw.mapreduce.core.MWReducer;

import java.util.Comparator;

import mw.mapreduce.core.MWContext.MWMapContext;
import mw.mapreduce.core.MWContext.MWReduceContext;
import mw.mapreduce.reader.MWReader;
import mw.mapreduce.util.MWPair;

public class MWFriendCountJob extends MWJob {

	public MWReducer createReducer(MWReduceContext context){
        return new MWFriendCountReducer(context);
    }

}
