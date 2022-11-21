package mw.mapreduce.reader;

import java.io.IOException;

import mw.mapreduce.util.MWPair;
import mw.mapreduce.util.MWSplitTextFileInput;

public class MWProfileReader implements MWReader<MWPair<String, String>>{

	private final String	ID_PATTERN = "id=\"next\"",
							FRIEND_PATTERN = "rel=\"friend\"",
							PROFILE_PATTERN = "<title>";

	private String line=null, key=null;
	private MWSplitTextFileInput mwSplitTextFileInput;

	public MWProfileReader(final String path, final long start, final long length){
		try {
			mwSplitTextFileInput = new MWSplitTextFileInput(path, start, length);
			// Zeilen solange überspringen, bis man auf Profilbeginn „<title>“ trifft. Der übersprungene Teil wird von vorherigem Mapper bearbeitet.
			while(true){
				line = mwSplitTextFileInput.readLine();
				if(!line.contains(PROFILE_PATTERN)){
					continue;
				}
				// System.out.println("LINE CONSTRUCTOR: " + line);
				break;												// In „line“ steht nun eine Zeile, die mit „<title>“ beginnt
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public MWPair<String, String> read() throws IOException {
		boolean stopAtNextProfile = false;
		while(true){
			line = mwSplitTextFileInput.readLine();
			/* Angefangenes Profil zu Ende parsen */
			if(line==null){									// mwSplitFileTextInput liefert „null“ zurück, weil Byte-Grenze überschritten wurde
				line=mwSplitTextFileInput.forceReadLine();	// alle spezifischen Bytes wurden gelesen aber Profil noch nicht zu Ende => ab jetzt muss man mit „forceReadLine()“ arbeiten, bis Profil abgeschlossen wurde
				if(line==null) return null;					// erhält man wieder „null“ gibt es in der Datei Nichts mehr einzulesen
				stopAtNextProfile = true;					// Markierung, dass bei der nächsten „<title>“-Zeile Bereich zu Ende gelesen wurde
			}
			/* ID extrahieren */
			if(key==null && line.contains(ID_PATTERN)){
				// System.out.println("LINE ID_PATTERN: " + line);
				key = getId(line, "value");
				continue;
			}
			/* Freund extrahieren */
			if(line.contains(FRIEND_PATTERN)){
				// System.out.println("LINE FRIEND_PATTERN: " + line);
				MWPair<String, String> p = new MWPair<String, String>(key, getId(line, "href"));
				return p;
			}
			/* Neues Profil beginnt */
			if(line.contains(PROFILE_PATTERN)){					// Stößt man wieder auf „<title>“, so ist Profil zu Ende und Neues beginnt, das beim nächsten Aufruf von read() direkt bearbeitet werden kann.
				// System.out.println("LINE PROFILE: " + line);
				key=null; 										// Eine neue Id muss gesucht werden, da neues Profil begonnen hat
				if(stopAtNextProfile){
					return null;								// Bereich zu Ende gelesen, neues Profil für nächsten Worker beginnt
				}
			}
		}
	}

	private String getId(final String line, final String indicator){		// Die Ids finden sich immer am Ende eines Links, der entweder unter „value“ (Profil-Id) oder "href" (Freund-Id) gespeichert wurde
		String[] splittedLine = line.split(" "),							//in „splittedLineWithFriendId“ stehen nun die Attribute
				 partsOfLink;
		String url = null, id;
		for(String s : splittedLine){										// Attribut „href“ extrahieren, da sich dort Link mit ID am Ende findet
			if(s.length() >= 4 && s.substring(0, indicator.length()).equals(indicator)){		// Manche Teile haben weniger als 4 Zeichen, deswegen zusätzliche Abfrage, ob Fehler zu vermeiden
				url = s.split("=")[1];
			}
		}
		// System.out.println("LINK: " + url);
		partsOfLink = url.split("/");
		id = partsOfLink[partsOfLink.length-1];								// „id“ findet sich nach dem letzten „/“
		id = id.substring(0, id.length()-1);								// enthält noch ein Anführungszeichen, das entfernt werden muss
		// System.out.println("ID: " + id);
		return id;
	}
}
