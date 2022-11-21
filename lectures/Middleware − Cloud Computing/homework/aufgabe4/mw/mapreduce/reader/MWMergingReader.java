package mw.mapreduce.reader;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import mw.mapreduce.util.MWPair;
import mw.mapreduce.util.MWSplitTextFileInput;

//Führt mehrere vorsortierte files zu einem zusammen, gibt diese Zeile für Zeile aus
public class MWMergingReader implements MWReader {

	// Vergleicht die beiden obersten Keys von zwei Listen aus MWPair
	public class ListComparator implements Comparator {

		private Comparator<String> clientComp;

		public ListComparator(Comparator<String> clientComp) {
			this.clientComp = clientComp;
		}

		@Override
		public int compare(Object arg0, Object arg1) {
			LinkedList<MWPair<String, String>> list1 = (LinkedList<MWPair<String, String>>) arg0;
			LinkedList<MWPair<String, String>> list2 = (LinkedList<MWPair<String, String>>) arg1;
			if(list1.isEmpty()) {
				if(list2.isEmpty())
					return 0;
				else
					return 1;
			}
			if(list2.isEmpty())
				return -1;
			// Vergleich der obersten Listenelementen mit dem MWPair-Comparator des Clients
			return clientComp.compare(list1.get(0).getKey(),list2.get(0).getKey());
		}
	}

	private PriorityQueue<LinkedList<MWPair<String, String>>> queue;


	public MWMergingReader(Iterable<String> inFiles, Comparator<String> c) throws IOException {

		queue = new PriorityQueue<LinkedList<MWPair<String, String>>>(new ListComparator(c));

		for (String filePath : inFiles) {	// Über alle Files iterieren

			System.out.println("MERGING READER: " + filePath);

			// Alle Schlüssel-Wert-Paare einer Mapper-Ausgabe werden in eigene Liste geschrieben und diese wiederum in der PriorityQueue
			LinkedList<MWPair<String, String>> list = new LinkedList<MWPair<String, String>>();

			// Zeile für Zeile lesen
			MWSplitTextFileInput reader = new MWSplitTextFileInput(filePath);
			String line = reader.readLine();
			while (line != null) {

				// Als MWPair in LinkedList einfügen, TODO: Zeilen ohne key-value aufbau?
				String[] splitted = line.split("\t");
				if (splitted.length == 2)
					list.add(new MWPair<String, String>(splitted[0], splitted[1]));

				line = reader.readLine();
			}
			// System.out.println("LIST: " + list.toString());

			// Vorsortierte Liste in PriorityQueue einfügen
			queue.add(list);
		}

		//System.out.println("QUEUE: " + queue.toString());
	}


	public MWPair<String, String> read2() throws IOException {

		// Liste holen
		LinkedList<MWPair<String, String>> list = queue.poll();
		if (list == null) return null;

		if(list.size()==0)
			return null;
		// Erstes Element in Liste entfernen
		MWPair<String, String> pair = list.remove(0);


		// Kleinere Liste wieder in Queue einfügen => PriorityQueue sortiert automatisch neu
		queue.add(list);

		return pair;
	}

	@Override
	public MWPair<String, Iterable<String>> read() throws IOException {
		String key=null;
		LinkedList<String> values = new LinkedList<String>();
		MWPair<String, String> pair;
		LinkedList<MWPair<String, String>> list;

		while(true){
			//System.out.println("QUEUE READ: " + queue.toString());
			list = queue.poll();
			if (list == null || list.size() == 0){
				if(values.size() == 0){
					return null;
				} else {
					return new MWPair<String, Iterable<String>>(key, values);
				}
			}
			pair = list.remove(0);
			if(key==null) key=pair.getKey();
			if(pair.getKey().equals(key)){
				//if(!values.contains(pair.getValue())){	// neuen Wert nur aufnehmen, wenn er noch nicht vorhanden ist
				//	System.out.println("LOREM");
				//	values.add(pair.getValue());
				//}
				values.add(pair.getValue());
				queue.add(list);	// kleinere Liste wieder in „queue“ einfügen
			} else {
				list.add(0, pair);		// Wert wieder in Liste einfügen
				queue.add(list);	// Liste wieder in PriorityQueue einfügen
				return new MWPair<String, Iterable<String>>(key, values);
			}
		}
	}

}
