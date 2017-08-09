package faultlocalization.api;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.github.javaparser.ParseException;

import faultlocalization.data.Ranking;
import faultlocalization.formulas.SpectrumBasedFormula.FORMULA;
import faultlocalization.utils.CompilationResult;
import faultlocalization.utils.CompilationUtils;
import faultlocalization.utils.FileUtils;
import faultlocalization.utils.StringUtils;

public class FaultLocalizationApi {
	
	private static boolean verbose = true;
	
	public static List<Ranking> createRankings(Request req) throws IOException, ParseException {
		List<Ranking> results = null;
		String clazzAsPath = FileUtils.classToPath(req.getClassToCover(), true);
		File input = new File(req.getSrcDir(), clazzAsPath);
		File output = new File(req.getOutDir(), clazzAsPath);
		RankingApi.instrumentCodeToCover(input, output, req.getID());
		CompilationResult cr = CompilationUtils.compile(output.getAbsolutePath(), Arrays.asList(req.getPaths()));
		String classFilePath = new File(req.getBinDir(), FileUtils.classToPath(req.getClassToCover(), false)).getAbsolutePath();
		if (cr.compilationSuccessful()) {
			if (!FileUtils.deactivateFile(classFilePath)) {
				System.err.println("Couldn't deactivate file " + classFilePath);
				return null;
			}
			if (verbose && cr.getWarnings() != null) System.err.println(cr.getWarnings());
			//create external process
			String[] args = getArgs(req);
			ProcessBuilder pb = new ProcessBuilder(args);
			File errorLog = new File("error.log");
			pb.redirectError(Redirect.appendTo(errorLog));
			try {
				Process p = pb.start();
				InputStream is = p.getInputStream();
				int exitCode = p.waitFor();
				//TODO: manage errors in the result
				if (exitCode != 0) {
					System.err.println("External JUnit runner failed with code " + exitCode);
				} else {
					if (is == null) {
						System.err.println("InputStream from external JUnit runner is null");
					} else {
						results = new LinkedList<>();
						results.addAll(parseResultsFromInputStream(is));
						is.close();
					}
				}
			} catch (IOException | InterruptedException | ClassNotFoundException e) {
				System.err.println(ExceptionUtils.getFullStackTrace(e));
			}
			if (!FileUtils.reactivateFile(classFilePath)) {
				System.err.println("Couldn't deactivate file " + classFilePath);
				return null;
			}
		} else {
			if (verbose) System.err.println(ExceptionUtils.getFullStackTrace(cr.error()));
		}
		return results;
	}
	
	private static Collection<? extends Ranking> parseResultsFromInputStream(InputStream is) throws ClassNotFoundException, IOException {
		List<Ranking> results = new LinkedList<>();
		ObjectInputStream in = new ObjectInputStream(is);
		Object o = null;
		try {
			while ((o = in.readObject()) != null) {
				Ranking r = (Ranking)o;
				results.add(r);
			}
		} catch (EOFException e) {}
		in.close();
		return results;
	}
	
	private static String[] getArgs(Request req) {
		//ProcessBuilder args are:
		//command + paths + instrumentedClassLocation(.class) + instrumentedClass + tests + formulas
		//where except for the command, the other arguments need a flag, which are the following:
		//-p -i -T -f
		String[] args = new String[req.getTests().length /*tests*/ + 4 /*command+args*/ + 4 /*flags*/ + 2 /*instrumentedClass (class+location)*/ + req.getFormulas().length /*formulas*/];
		args[0] = "java";
		args[1] = "-cp";
		args[2] = StringUtils.arrayToString(req.getPaths(), File.pathSeparator);
		args[3] = "faultlocalization.junit.runner.ExternalJUnitTestRunner";
		args[4] = "-d";
		args[5] = req.getID().toString();
		args[6] = "-i";
		args[7] = req.getClassToCover();
		args[8] = "-T";
		int i = 1;
		for (String t : req.getTests()) {
			args[8+i] = t;
			i++;
		}
		args[8+i] = "-f";
		int j = 1;
		for (FORMULA f : req.getFormulas()) {
			args[8+i+j] = f.toString();
			j++;
		}
		return args;
	}

}
