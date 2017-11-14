package faultlocalization.coverage;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class holds the coverage information for the spectrum-based fault localization techniques
 * 
 * @author stein
 * @version 0.1
 */
public class CoverageInformation implements Serializable {

	private static final long serialVersionUID = -2755642329685340486L;
	private Map<Integer, Integer> positiveTestsPerLine;
	private Map<Integer, Integer> negativeTestsPerLine;
	private Set<Integer> markedLines;
	private Set<Integer> visitedLines;
	private int totalPassedtests = 0;
	private int totalFailedTests = 0;
	
	
	public CoverageInformation() {
		this.positiveTestsPerLine = new TreeMap<>();
		this.negativeTestsPerLine = new TreeMap<>();
		this.visitedLines = new TreeSet<>();
		this.markedLines = new TreeSet<>();
	}
	
	public void mark(int line) {
		this.visitedLines.add(line);
		this.markedLines.add(line);
	}
	
	public <T> T mark(int line, T expression) {
		mark(line);
		return expression;
	} 
	
	public void applyMarkedLines(boolean passed) {
		Map<Integer, Integer> m = passed?this.positiveTestsPerLine:this.negativeTestsPerLine;
		for (Integer l : visitedLines) {
			Integer value = 0;
			if (m.containsKey(l)) {
				value = m.get(l);
			}
			m.put(l, value+1);
		}
		this.visitedLines.clear();
		if (passed) this.totalPassedtests++;
		else this.totalFailedTests++;
	}
	
	public int getPassedCount(int line) {
		if (this.positiveTestsPerLine.containsKey(line)) {
			return this.positiveTestsPerLine.get(line);
		}
		return 0;
	}
	
	public int getFailedCount(int line) {
		if (this.negativeTestsPerLine.containsKey(line)) {
			return this.negativeTestsPerLine.get(line);
		}
		return 0;
	}
	
	public int getExecutedTimes(int line) {
		return getFailedCount(line) + getPassedCount(line);
	}
	
	public Set<Integer> getMarkedLines() {
		return this.markedLines;
	}
	
	public int getTotalFailedTests() {
		return this.totalFailedTests;
	}
	
	public int getTotalPassedTests() {
		return this.totalPassedtests;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("positive tests per line\n");
		Iterator<Entry<Integer, Integer>> pti = positiveTestsPerLine.entrySet().iterator();
		while (pti.hasNext()) {
			Entry<Integer, Integer> c = pti.next();
			sb.append(c.getKey() + " : " + c.getValue());
			if (pti.hasNext()) {
				sb.append("\n");
			}
		}
		sb.append("\n");
		sb.append("negative tests per line\n");
		Iterator<Entry<Integer, Integer>> nti = negativeTestsPerLine.entrySet().iterator();
		while (nti.hasNext()) {
			Entry<Integer, Integer> c = nti.next();
			sb.append(c.getKey() + " : " + c.getValue());
			if (nti.hasNext()) {
				sb.append("\n");
			}
		}
		sb.append("\n");
		sb.append("marked lines : ");
		Iterator<Integer> mli = markedLines.iterator();
		while (mli.hasNext()) {
			Integer c = mli.next();
			sb.append(c);
			if (mli.hasNext()) {
				sb.append(",");
			}
		}
		sb.append("\n");
		sb.append("total passed tests: " + totalPassedtests + "\n");
		sb.append("total failed tests: " + totalFailedTests + "\n");
		return sb.toString();
	}

}
