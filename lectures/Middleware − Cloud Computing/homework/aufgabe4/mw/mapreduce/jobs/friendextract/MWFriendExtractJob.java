package mw.mapreduce.jobs.friendextract;

import mw.mapreduce.core.MWJob;
import mw.mapreduce.core.MWMapper;
import mw.mapreduce.core.MWContext.MWMapContext;
import mw.mapreduce.reader.MWProfileReader;
import mw.mapreduce.reader.MWReader;
import mw.mapreduce.util.MWPair;

import mw.mapreduce.core.MWReducer;
import mw.mapreduce.core.MWContext.MWReduceContext;

import mw.mapreduce.jobs.friendextract.MWFriendExtractMapper;
import mw.mapreduce.jobs.friendextract.MWFriendExtractReducer;

public class MWFriendExtractJob extends MWJob {

	public MWReader<MWPair<String, String>> createInputReader(final String path, final long start, final long length){
		return new MWProfileReader(path, start, length);
	}

	public MWMapper createMapper(MWMapContext context) {
		return new MWFriendExtractMapper(context);
	}

	public MWReducer createReducer(MWReduceContext context) {
		MWReducer reducer = new MWFriendExtractReducer(context);
		return reducer;
	}
		// Spezieller Comparator auch nicht notwendig, da nicht sortiert werden soll.

}
