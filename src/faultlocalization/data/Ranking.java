package faultlocalization.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import faultlocalization.coverage.CoverageInformation;
import faultlocalization.formulas.SpectrumBasedFormula;
import faultlocalization.formulas.SpectrumBasedFormula.FORMULA;

public class Ranking implements Serializable {

	private static final long serialVersionUID = -6229081020514172753L;
	
	private transient Map<Integer, Float> rankedStatements;
	private transient Map<Integer, Float> normalizedRankStatements;
	private transient Map<Integer, Float> normalizedRrankingProbabilities;
	private final FORMULA formula;
	private final CoverageInformation ci;
	
	public Ranking(CoverageInformation ci, FORMULA formula) {
		this.ci = ci;
		this.formula = formula;
	}
	
	public Map<Integer, Float> getRankedStatements() {
		if (rankedStatements == null) {
			recalculateRanking();
		}
		return rankedStatements;
	}

	private void recalculateRanking() {
		rankedStatements = SpectrumBasedFormula.rankStatements(ci, formula);
	}
	
	public Map<Integer, Float> getNormalizedRankStatements() {
		normalizedRankStatements = new TreeMap<>();
		Float minSuspiciouness = (float) 1;
		Float maxSuspiciouness = (float) 0;
		float normalizedSuspiciounessSum = 0;
		Float EPSILON = (float) 1.0E-17;
		
		for (Integer l : ci.getMarkedLines()) {
			int pos = ci.getPassedCount(l);
			int neg = ci.getFailedCount(l);
			int totalPos = ci.getTotalPassedTests();
			int totalNeg = ci.getTotalFailedTests();
			float suspiciounessValue = this.formula.evaluate(pos, neg, totalPos, totalNeg);
			normalizedRankStatements.put(l,suspiciounessValue);
			if(suspiciounessValue < minSuspiciouness)
				minSuspiciouness = suspiciounessValue;
			if(suspiciounessValue > maxSuspiciouness)
				maxSuspiciouness = suspiciounessValue;
		}
		float normalizingTerm = maxSuspiciouness - minSuspiciouness;
		
		normalizedRankStatements = sortByValue(normalizedRankStatements);
		
		for (Integer l :ci.getMarkedLines() ){
			float normalizedSuspiciouness = (normalizedRankStatements.get(l) - minSuspiciouness)/(normalizingTerm);
			normalizedRankStatements.put(l,((normalizedSuspiciouness != 0)?normalizedSuspiciouness:EPSILON));
			normalizedSuspiciounessSum += normalizedSuspiciouness;
		}
		
		// compute normalized ranking probabilities
		
		normalizedRrankingProbabilities = new TreeMap<>();
		for (Integer l :ci.getMarkedLines() ){
			normalizedRrankingProbabilities.put(l, (normalizedRankStatements.get(l)/normalizedSuspiciounessSum));
		}
		
		normalizedRrankingProbabilities = sortByValue(normalizedRrankingProbabilities);
		
		//return sortByValueAndExecutedTimes(ranking, coverageInfo);
		return sortByValue(normalizedRankStatements);
	}
	
	/*
	 * @requires normalizedRankStatements was executed 
	 * @throws IllegalStateException if normalizedRankStatements method didn't execute
	 */
	public Map<Integer, Float> getProbabilitiesRanking() throws IllegalStateException{
		if (normalizedRrankingProbabilities==null)
			throw new IllegalStateException("didn't run normalizedRankStatements method");
		return normalizedRrankingProbabilities;
	}
	
	public FORMULA getFormula() {
		return formula;
	}
	
	public CoverageInformation getCI() {
		return ci;
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
	
	@Override
	public String toString() {
		String res = formula.getName() + "\n";
		res += "Description : " + formula.description() + "\n";
		res += "------------------------------\n";
		for (Entry<Integer, Float> rank : getRankedStatements().entrySet()) {
			System.out.println("s : " + rank.getKey() + " r : " + rank.getValue() + " executed times : " + ci.getExecutedTimes(rank.getKey()) );
		}
		return res;
	}

}
