package faultlocalization.formulas;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import faultlocalization.coverage.CoverageInformation;


public class SpectrumBasedFormula {
	public static enum FORMULA {
		
		TARANTULA {

			@Override
			public String getName() {
				return "TARANTULA";
			}

			@Override
			public String description() {
				return "TARANTULA IS VERY CREEPY";
			}
			
			@Override
			public Float evaluate(int positive, int negative, int totalPositive, int totalNegative) {
				return (negative/(float)totalNegative)/((negative/(float)totalNegative)+(positive/(float)totalPositive));
			}
			
		},
		
		OCHIAI {

			@Override
			public String getName() {
				return "OCHIAI";
			}

			@Override
			public String description() {
				return "OCHIAI sounds like an asian te";
			}
			
			@Override
			public Float evaluate(int positive, int negative, int totalPositive, int totalNegative) {
				return negative / (float) Math.sqrt(totalNegative * (negative + positive));
			}
			
		},
		
		OP2 {

			@Override
			public String getName() {
				return "OP2";
			}

			@Override
			public String description() {
				return "It comes next to OP1";
			}
			
			@Override
			public Float evaluate(int positive, int negative, int totalPositive, int totalNegative) {
				return negative - (positive / (float)(totalPositive+1));
			}
			
		},
		
		BARINEL {

			@Override
			public String getName() {
				return "BARINEL";
			}

			@Override
			public String description() {
				return "BARINEL is another way to write BAR LINE but a wrong way";
			}
			
			@Override
			public Float evaluate(int positive, int negative, int totalPositive, int totalNegative) {
				return 1 - (positive/(float)(positive+negative));
			}
			
		},
		
		DSTAR {

			@Override
			public String getName() {
				return "DSTAR";
			}

			@Override
			public String description() {
				return "The Star!";
			}
			
			@Override
			public Float evaluate(int positive, int negative, int totalPositive, int totalNegative) {
				return (float) (Math.pow(negative, 2) / (float)(positive+(totalNegative - negative))); 
			}
			
		};
		
		public abstract String getName();
		public abstract String description();
		public abstract Float evaluate(int positive, int negative, int totalPositive, int totalNegative);
	}
	
	public static Map<Integer, Float> rankStatements(CoverageInformation coverageInfo, FORMULA f) {
		Map<Integer, Float> ranking = new TreeMap<>();
		for (Integer l : coverageInfo.getMarkedLines()) {
			int pos = coverageInfo.getPassedCount(l);
			int neg = coverageInfo.getFailedCount(l);
			int totalPos = coverageInfo.getTotalPassedTests();
			int totalNeg = coverageInfo.getTotalFailedTests();
			ranking.put(l, f.evaluate(pos, neg, totalPos, totalNeg));
		}
		return sortByValueAndExecutedTimes(sortByValue(ranking), coverageInfo);
	}
	
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
	    return map.entrySet()
	              .stream()
	              .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
	              .collect(Collectors.toMap(
	                Map.Entry::getKey, 
	                Map.Entry::getValue, 
	                (e1, e2) -> e1, 
	                LinkedHashMap::new
	              ));
	}
	
	/*
	 * Reorder ranking by line execution times, only when a neighbor pair of entries have a same ranking value.
	 */
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAndExecutedTimes(Map<K, V> map, CoverageInformation ci) {
		Map<K, V> ranking = new LinkedHashMap<>();
		Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
		while ( iterator.hasNext()) {
			Entry<K, V>  entry = iterator.next();
			K currentLine = entry.getKey();
			if (iterator.hasNext()) {
				Entry<K, V> nextEntry = iterator.next();
				K nextLine = nextEntry.getKey();
				int entryTimes = ci.getExecutedTimes((int) currentLine);
				int nextEntryTimes = ci.getExecutedTimes((int) nextLine);
				if (entry.getValue().compareTo(nextEntry.getValue()) == 0 && entryTimes < nextEntryTimes) {
					ranking.put(nextLine, nextEntry.getValue());
					ranking.put(currentLine, entry.getValue());
				}else{
					ranking.put(currentLine, entry.getValue());
					ranking.put(nextLine, nextEntry.getValue());
				}	
			}else{
				ranking.put(currentLine, entry.getValue());
			}
		}
		return ranking;
	}

}
