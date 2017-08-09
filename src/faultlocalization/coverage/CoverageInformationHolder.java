package faultlocalization.coverage;

import java.util.Map;
import java.util.TreeMap;

public class CoverageInformationHolder {
	
	private Map<Long, CoverageInformation> coverageHolder;
	private static CoverageInformationHolder instance;
	
	public static CoverageInformationHolder getInstance() {
		if (instance == null)
			instance = new CoverageInformationHolder();
		return instance;
	}
	
	private CoverageInformationHolder() {
		this.coverageHolder = new TreeMap<>();
	}
	
	public void instantiateCoverageInformation(Long id) {
		this.coverageHolder.put(id, new CoverageInformation());
	}
	
	public CoverageInformation getCoverageInformation(Long id) {
		return coverageHolder.get(id);
	}

}
