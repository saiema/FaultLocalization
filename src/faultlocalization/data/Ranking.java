package faultlocalization.data;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import faultlocalization.coverage.CoverageInformation;
import faultlocalization.formulas.SpectrumBasedFormula;
import faultlocalization.formulas.SpectrumBasedFormula.FORMULA;

public class Ranking implements Serializable {

	private static final long serialVersionUID = -6229081020514172753L;
	
	private transient Map<Integer, Float> rankedStatements;
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
	
	public FORMULA getFormula() {
		return formula;
	}
	
	public CoverageInformation getCI() {
		return ci;
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
