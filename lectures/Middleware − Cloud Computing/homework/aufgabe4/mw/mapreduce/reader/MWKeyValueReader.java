package mw.mapreduce.reader;

import java.io.IOException;

import mw.mapreduce.util.MWPair;
import mw.mapreduce.util.MWSplitTextFileInput;

public class MWKeyValueReader implements MWReader<MWPair<String, String>> {

	private volatile int lineNumber = 0;
	String filePath;
	private MWSplitTextFileInput reader;
	long start;
	long length;
	MWSplitTextFileInput mwSplitTextFileInput;

	public MWKeyValueReader(String path) throws IOException {
		this.filePath = path;

	}

	public MWKeyValueReader(String path, long start, long length) throws IOException {
		this.filePath = path;
		this.start = start;
		this.length = length;

	}

	// synchronized, da nur ein Thread lesen darf
	@Override
	public synchronized MWPair<String, String> read() throws IOException {

		// Auslesen und splitten
		if (reader == null) {
			if (start == 0 && length == 0)
				reader = new MWSplitTextFileInput(filePath);
			else {
				reader = new MWSplitTextFileInput(filePath, start, length);
			}
		}

		String line = null;

		line = reader.readLine();

		if (line == null)
			return null;

		String[] splitted = line.split("\\t");

		switch (splitted.length) {
		case 0:
			return null;
		case 1:
			lineNumber++;
			return new MWPair<String, String>(lineNumber + "", splitted[0]);
		case 2:
			lineNumber++;
			return new MWPair<String, String>(splitted[0], splitted[1]);
		default:// TODO: Was tun bei mehreren \t in einer Zeile?
			System.err.println("Mehrere \t");
			break;
		}

		lineNumber++;
		return null;
	}
}
