package faultlocalization.api;

import faultlocalization.formulas.SpectrumBasedFormula.FORMULA;
import static faultlocalization.utils.FileUtils.*;
import static faultlocalization.utils.FileUtils.CHECK.*;

public class Request {
	private final String classToCover;
	private final String srcDir;
	private final String binDir;
	private final String[] paths;
	private final FORMULA[] formulas;
	private final String outDir;
	private final String[] tests;
	private final Long id;

	public Request(String classToCover, String srcDir, String binDir, String outDir, String[] paths, FORMULA[] formulas, String[] tests, Long id) {
		this.classToCover = classToCover;
		this.srcDir = srcDir;
		if (!checkPathProperties(srcDir, EXISTS, IS_DIRECTORY)) {
			throw new IllegalArgumentException("Source directory " + srcDir + " doesn't exist or is not a directory");
		}
		this.binDir = binDir;
		if (!checkPathProperties(binDir, EXISTS, IS_DIRECTORY)) {
			throw new IllegalArgumentException("Binary directory " + binDir + " doesn't exist or is not a directory");
		}
		if (!containsClass(srcDir, classToCover, true)) {
			throw new IllegalArgumentException("No java file for class " + classToCover + " in source directory");
		}
		this.outDir = outDir;
		if (!checkPathProperties(outDir, EXISTS, IS_DIRECTORY)) {
			throw new IllegalArgumentException("Output directory " + outDir + " is a file");
		}
		this.paths = paths;
		for (String p : paths) {
			if (!checkPathProperties(p, EXISTS)) {
				throw new IllegalArgumentException("path " + p + " doesn't exist");
			}
			CHECK jarCheck = EXTENSION;
			jarCheck.setValue("jar");
			if (!checkPathProperties(p, IS_DIRECTORY) && !checkPathProperties(p, jarCheck)) {
				throw new IllegalArgumentException("path " + p + " is neither a directory nor a jar file");
			}
		}
		if (formulas == null || formulas.length == 0) {
			throw new IllegalArgumentException("null or empty formulas");
		}
		if (tests == null || tests.length == 0) {
			throw new IllegalArgumentException("null or empty tests");
		}
		for (String t : tests) {
			if (!containsClass(paths, t, false)) {
				throw new IllegalArgumentException("Class file for test " + t + " can't be found in any of the provided paths");
			}
		}
		this.tests = tests;
		this.formulas = formulas;
		this.id = id;
	}

	public String getClassToCover() {
		return classToCover;
	}

	public String getSrcDir() {
		return srcDir;
	}

	public String getBinDir() {
		return binDir;
	}

	public String[] getPaths() {
		return paths;
	}

	public FORMULA[] getFormulas() {
		return formulas;
	}

	public String getOutDir() {
		return outDir;
	}
	
	public String[] getTests() {
		return tests;
	}
	
	public Long getID() {
		return id;
	}
	
}
