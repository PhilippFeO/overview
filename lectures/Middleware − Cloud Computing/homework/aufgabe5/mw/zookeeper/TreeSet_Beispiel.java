package mw.zookeeper;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class TreeSet_Beispiel {
	public static void main(String[] args){
		TreeSet<String> tree = new TreeSet<String>(new NodeComparator2());
		tree.add("/leader");
		tree.add("/leader/1");
		//tree.add("/");
		tree.add("/first");
		tree.add("/leader/2");
		tree.add("/other");
		String t = "/leader";
		char c = t.toCharArray()[1];
		System.out.println(++c);
		SortedSet<String> s = tree.subSet("/leader", "/leader/");
		for(String ss : s) System.out.println(ss);

	}
}


class NodeComparator2 implements Comparator<String>{
	@Override
	public int compare(final String o1, final String o2) {
		return o1.compareTo(o2);
	}

}