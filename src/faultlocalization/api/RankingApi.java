package faultlocalization.api;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.runners.model.InitializationError;

import com.github.javaparser.ParseException;

import faultlocalization.coverage.CoverageInformation;
import faultlocalization.coverage.CoverageInformationHolder;
import faultlocalization.coverage.Instrumenter;
import faultlocalization.data.Ranking;
import faultlocalization.formulas.SpectrumBasedFormula.FORMULA;
import faultlocalization.junit.runner.FaultLocalizationTrigger;
import faultlocalization.junit.runner.JunitTestRunner;
import faultlocalization.junit.runner.TestRunnerException;
import faultlocalization.utils.FileUtils;

public class RankingApi {
	
	public static void instrumentCodeToCover(File original, File output, long id) throws IOException, ParseException {
		FileUtils.createNewFile(output, true);
		Instrumenter instrumentalizator = new Instrumenter(original, id);
		instrumentalizator.instrument(output);
	}
	
	public static List<Ranking> createRankings(String classToCover, Long id, String[] junitTests, FORMULA... formulas) {
		CoverageInformationHolder.getInstance().instantiateCoverageInformation(id);
		FaultLocalizationTrigger trigger = new FaultLocalizationTrigger(CoverageInformationHolder.getInstance().getCoverageInformation(id));
		for (String test : junitTests) {
			Class<?> testToRun;
			try {
				System.err.println("class to cover loaded from: " + Class.forName(classToCover).getClassLoader().getResource(FileUtils.classToPath(classToCover, false).toString()));
				testToRun = Class.forName(test);
				
				JunitTestRunner testRunner = new JunitTestRunner(testToRun, trigger);
				testRunner.run();
				killStillRunningJUnitTestcaseThreads();
				
				
			} catch (TestRunnerException | InitializationError | ClassNotFoundException e) {
				System.err.println(ExceptionUtils.getFullStackTrace(e));
			}
		}
		
		List<Ranking> results = new LinkedList<>();
		
		
		CoverageInformation ci = CoverageInformationHolder.getInstance().getCoverageInformation(id);
		
		for (FORMULA f : formulas) {
			results.add(new Ranking(ci, f));
		}
		
		return results;
	}
	
	@SuppressWarnings("deprecation")
	private static void killStillRunningJUnitTestcaseThreads() {
	    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
	    for (Thread thread : threadSet) {
	        if (!(thread.isDaemon())) {
	            final StackTraceElement[] threadStackTrace = thread.getStackTrace();
	            if (threadStackTrace.length > 1) {
	                StackTraceElement firstMethodInvocation = threadStackTrace[threadStackTrace.length - 1];
	                if (firstMethodInvocation.getClassName().startsWith("org.junit")) {
	                    // HACK: must use deprecated method
	                    thread.stop();
	                }
	            }
	        }
	    }
	}

}
