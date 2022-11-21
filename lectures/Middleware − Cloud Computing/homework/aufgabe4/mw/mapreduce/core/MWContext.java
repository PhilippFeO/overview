package mw.mapreduce.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Comparator;

import mw.mapreduce.reader.MWReader;
import mw.mapreduce.util.MWPair;

public interface MWContext<VALUE> {
	//Input
	MWReader<MWPair<String, VALUE>> getReader();

	//Output
	void write(String key, String value) throws IOException;
	void outputComplete() throws IOException;

	public class MWMapContext implements MWContext {

		// Keine Verwendung von Hash*-Datenstrukturen, weil man meist die Schlüssel nur als Set extrahieren kann und dann mehrfaches Auftreten eines Schlüssels unter den Tisch fällt
		private final LinkedList<MWPair<String, String>> RESULTS = new LinkedList<MWPair<String, String>>();
		private final LinkedList<String> KEYS = new LinkedList<String>();	// eigene Liste für die Schlüssel, damit man diese Sortieren

		MWReader<MWPair<String, String>> reader;
		private Comparator<String> c;
		private int numPartitions;
		private String dir;
		private String prefix;

		public MWMapContext(MWReader<MWPair<String, String>> reader, final String dir, final String prefix, final int numReducer, final Comparator<String> comparator){
			this.reader = reader;
			this.c = comparator;
			this.numPartitions = numReducer;
			this.dir = dir==null ? "./mw/mapreduce/core/MapperOutput/" : dir;
			this.prefix = prefix==null ? "map-" : prefix;
		}

		@Override
		public MWReader<MWPair<String, String>> getReader() {
			return reader;
		}

		@Override
		public void write(final String key, final String value) throws IOException {
			MWPair<String, String> candidate = new MWPair<String, String>(key, value);
			//System.out.println(RESULTS.contains(candidate));
			//if(!RESULTS.contains(candidate)){
				//System.out.println("CONTEXT WRITE PAIR: " + candidate.toString());
					RESULTS.add(candidate);						// „sammeln“ der Resultate; sortieren, partitionieren, etc. in „outputComplete()“
					KEYS.add(key);																// eigene Liste für die Schlüssel, damit man diese Sortieren
			//}
		}

		@Override
		public void outputComplete() throws IOException {
			/* Sortieren */
			KEYS.sort(c);

			/* Partitionieren */
			final LinkedList<LinkedList<MWPair<String, String>>> partitions = new LinkedList<LinkedList<MWPair<String, String>>>();	// Jede Liste beschreibt eine Partition, die dann in eine Datei geschrieben wird.
				// Partitionen initialisieren
			for(int i = 0; i < numPartitions; i++){
				partitions.add(new LinkedList<MWPair<String, String>>());						// Leere Partitionen hinzufügen, damit „get(<INDEX PARTITION>)“ nicht fehlschlägt/möglich ist
			}
				// Partitionen füllen
			for(String key : KEYS){
				int partition = (key.hashCode() & Integer.MAX_VALUE) % numPartitions;			// Ausrechnen der Partition für jedes Resultat
				partitions.get(partition).add(removeFirstOccurenceOfKey(key));					// Das erste Auftreten des Schlüssels wird aus „RESULTS“ entfernt und in der entsprechenden Partition gespeichert
			}

			for(LinkedList<MWPair<String, String>> partition : partitions){
				String filename = dir + prefix + "-" + Integer.toString(partitions.indexOf(partition));	// filename = <Prefix>-<MapperID>-<Partition=ReducerID>
				File f = new File(filename);
				if(f.exists()) {
					// wipe file
					PrintWriter writer = new PrintWriter(f);
					writer.print("");
					writer.close();
				} else {
					System.out.println(filename);
					f.createNewFile();
				}

				for(MWPair<String, String> pair : partition){
					try{
						Files.writeString(Paths.get(filename), pair.toString() + "\n", StandardOpenOption.APPEND);
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			}
		}

		/* Entfernt das erste auftrende Schlüssel-Wert-Paar mir Schlüssel „key“ aus „RESULTS“ und gibt es zurück */
		private MWPair<String, String> removeFirstOccurenceOfKey(final String key){
			for(MWPair<String, String> res : RESULTS)					// Durchsuchen der Paar-Liste nach dem ersten Auftreten des übergebeben Schlüssels „key“
				if(res.getKey().equals(key))
					return RESULTS.remove(RESULTS.indexOf(res));
			return null;
		}

	}

	public class MWReduceContext implements MWContext {
		private final LinkedList<MWPair<String, String>> results = new LinkedList<MWPair<String, String>>();
		String reducerID;
		private String dir;
		private String prefix;
		private String filepath;
		MWReader<MWPair<String, Iterable<String>>> reader;

		public MWReduceContext(MWReader<MWPair<String, Iterable<String>>> reader, final String id, final String dir, final String prefix){
			this.reader = reader;
			this.reducerID = id;
			this.dir = dir;
			this.prefix = prefix;
			this.filepath = dir + prefix + "-" + id;

			// TODO alte Dateien Löschen?
        	/* Erstelle Datei für den Reducer, später (dh. im Context) wird nur angehängt */
			try {
				Files.writeString(
					Paths.get(filepath),
					"",
					StandardOpenOption.CREATE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public MWReader<MWPair<String, Iterable<String>>> getReader() {
			return reader;
		}

		@Override
		public void write(String key, String value) throws IOException {
			results.add(new MWPair<String, String>(key, value));
		}

		@Override
		public void outputComplete() throws IOException {
			for(MWPair<String, String> res : results){
				Files.writeString(
					Paths.get(filepath),
					res.toString() + "\n",
					StandardOpenOption.APPEND);
			}
		}

	}

}
