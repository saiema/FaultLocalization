package faultlocalization.api;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.github.javaparser.ParseException;

import faultlocalization.coverage.Instrumenter;
import faultlocalization.data.Ranking;

public class MuJavaApi {
	
	public static File generateMutGenLimitVersion(	String className, File fcodeFolder, File outputFolder, int line) throws IOException, ParseException {
			return generateMutGenLimitVersion(className, fcodeFolder, outputFolder, line, 5);
	}

	public static File generateMutGenLimitVersion( String className, File fcodeFolder, File outputFolder, int line, int mgl) throws IOException, ParseException {
		String classNameAsPath = className.replaceAll("\\.", File.separator);
		File classFile = fcodeFolder.toPath().resolve(classNameAsPath + ".java").toFile();
		File outputMGLFolder = outputFolder.toPath().resolve(className+"-"+line).toFile();
		File outputFile = outputMGLFolder.toPath().resolve(classNameAsPath+".java").toFile();
		if (outputFile.exists()) outputFile.delete();
		outputFile.getParentFile().mkdirs();
		outputFile.createNewFile();
		
		Map<Integer, Integer> mglPerLine = new TreeMap<>();
		mglPerLine.put(line, mgl);
		Instrumenter instrumentalizator = new Instrumenter(classFile, mglPerLine);
		instrumentalizator.instrument(outputFile);
		return outputFile;
	}

	public static File generateMutGenLimitMultiVersion( String className, File fcodeFolder, File outputFolder, Map<Integer, Integer> mutGenLimitsPerLine) throws IOException, ParseException {
		String classNameAsPath = className.replaceAll("\\.", File.separator);
		String fileName = className;
		for (Entry<Integer, Integer> l : mutGenLimitsPerLine.entrySet()) {
			fileName += "_" + l.getKey() + "-" + l.getValue();
		}
		File classFile = fcodeFolder.toPath().resolve(classNameAsPath + ".java").toFile();	
		File outputMGLFolder = outputFolder.toPath().resolve(fileName).toFile();
		File outputFile = outputMGLFolder.toPath().resolve(classNameAsPath+".java").toFile();
		if (outputFile.exists()) outputFile.delete();
		outputFile.getParentFile().mkdirs();
		outputFile.createNewFile();
		
		Instrumenter instrumentalizator = new Instrumenter(classFile, mutGenLimitsPerLine);
		instrumentalizator.instrument(outputFile);
		return outputFile;
	}
	
	public static boolean generateMutGenLimitVersions(Ranking r, File sourceFolder, File outputFolder, String clazz, int rankedStatements, int min, int max) throws IOException, ParseException {
		Set<Integer> linesToMark = new TreeSet<>();
		Map<Integer, Float> ranking = r.getRankedStatements();
		for (Entry<Integer, Float> re : ranking.entrySet()) {
			if (re.getValue() > 0 && !linesToMark.contains(re.getKey())) {
				linesToMark.add(re.getKey());
			}
		}
		
		System.out.println("lines to mark: "+linesToMark.size());
		
		if (!linesToMark.isEmpty()) {
			List<Integer> orderedLines = new LinkedList<>(linesToMark);
			
			orderedLines = orderedLines.subList(0, Math.min(orderedLines.size(), rankedStatements));
			
			Map<Integer, List<Map<Integer, Integer>>> mglVersionsPerLine = new TreeMap<>();
			for (Integer line : orderedLines) {
				mglVersionsPerLine.put(line, generateMGLPerLine(line, min, max, null));
			}
			
			List<Map<Integer, Integer>> mutGenLimitVersions = new LinkedList<>();
			
			List<Map<Integer, Integer>> lastMGLVersions = new LinkedList<>();
			for (int l = 0 ; l < orderedLines.size(); l++) {
				int line = orderedLines.get(l);
				List<Map<Integer, Integer>> currentLineMGLVersions = mglVersionsPerLine.get(line);
				if (!lastMGLVersions.isEmpty()) {
					List<Map<Integer, Integer>> newLastMGLVersions = new LinkedList<>();
					for (Map<Integer, Integer> lastMGLVersion : lastMGLVersions) {
						List<Map<Integer, Integer>> newMultiLineVersions = generateMGLPerLine(line, min, max, lastMGLVersion);
						newLastMGLVersions.add(newMultiLineVersions.get(newMultiLineVersions.size() - 1));
						mutGenLimitVersions.addAll(newMultiLineVersions);
					}
					lastMGLVersions.addAll(newLastMGLVersions);
				}
				mutGenLimitVersions.addAll(currentLineMGLVersions);
				lastMGLVersions.add(currentLineMGLVersions.get(currentLineMGLVersions.size() - 1));
			}
			
			for (Map<Integer, Integer> mglv : mutGenLimitVersions) {
				System.out.println("===============================");
				for (Entry<Integer, Integer> lineMGL : mglv.entrySet()) {
					System.out.println("Line : " + lineMGL.getKey() + "      |     mutGenLimit : " + lineMGL.getValue());
				}
			}
			
			int idx = 0;
			for (Map<Integer, Integer> mglv : mutGenLimitVersions) {
				File out = outputFolder.toPath().resolve("MutGenLimitVersions").resolve(String.valueOf(idx)).toFile();
				File markedFile = generateMutGenLimitMultiVersion(clazz, sourceFolder, out, mglv);
				System.out.println("Marked version saved to : " + markedFile.getPath());
				idx++;
			}
			
		} else {
			System.err.println("lines to mark is empty!!!!");
			return false;
		}
		return true;
	}
	
	private static List<Map<Integer, Integer>> generateMGLPerLine(int line, int min, int max, Map<Integer, Integer> lastMGLVersion) {
		List<Map<Integer,Integer>> result = new LinkedList<>();
		for (int v = min; v <= max; v++) {
			Map<Integer, Integer> lineMgl = new TreeMap<>();
			lineMgl.put(line, v);
			if (lastMGLVersion != null) {
				lineMgl.putAll(lastMGLVersion);
			}
			result.add(lineMgl);
		}
		return result;
	}

}
