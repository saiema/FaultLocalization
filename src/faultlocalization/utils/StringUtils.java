package faultlocalization.utils;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class StringUtils {
	
	public static String listToString(List<String> l) {
		return listToString(l, " ");
	}
	
	public static String listToString(List<String> l, String separator) {
		StringJoiner sj = new StringJoiner(separator);
		l.stream().forEach(e -> sj.add(e));
		return sj.toString();
	}
	
	public static String arrayToString(String[] a, String separator) {
		return listToString(Arrays.asList(a), separator);
	}

}
