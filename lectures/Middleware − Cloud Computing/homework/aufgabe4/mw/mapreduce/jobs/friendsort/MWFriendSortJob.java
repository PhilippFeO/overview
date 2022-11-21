package mw.mapreduce.jobs.friendsort;

import java.util.Comparator;

import mw.mapreduce.core.MWJob;
import mw.mapreduce.core.MWMapper;
import mw.mapreduce.core.MWContext.MWMapContext;

public class MWFriendSortJob extends MWJob {

	class StringReverseComparator implements Comparator<String> {
        public int compare(String obj1, String obj2) {
            if (obj1 == obj2) {
                return 0;
            }
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
			}
			// VZ, nicht Zahl an sich ausschlaggebend
            return -obj1.compareTo(obj2);		// Lexikografische Sortierung soll einfach umgekehrt werden => „-1“ als Faktor hinzufügen
        }
	}

	// Als InputReader für den Mapper kann der Standard-Reader „MWKeyValueReader“ verwendet werden
	// Als Reducer kann der Standard-Reducer „MWReducer“ verwendet werden

	public MWMapper createMapper(MWMapContext context){
		return new MWFriendSortMapper(context);
	}

	public java.util.Comparator<String> getComparator(){
        return new StringReverseComparator();
    }

	/*
	public static void main(String[] argv){
		StringComparator s = (new MWFriendSortJob()).new StringComparator();
		System.out.println(s.compare("123", "789")); // Comparator liefert pos. VZ => „rechts“ größer als „links“
	}
	*/
}
