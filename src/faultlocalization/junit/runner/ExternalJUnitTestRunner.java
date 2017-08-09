package faultlocalization.junit.runner;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.exception.ExceptionUtils;
import faultlocalization.api.RankingApi;
import faultlocalization.data.Ranking;
import faultlocalization.formulas.SpectrumBasedFormula.FORMULA;

/**
 * This class is meant as an alternative to running fault localization rankings using the Reloader.
 * 
 * @author stein
 * @version 0.1
 */
public class ExternalJUnitTestRunner {
	private static final String VERSION = "0.1";
	
	/**
	 * Main for running fault localization on an already instrumented class
	 * 
	 * exit codes :
	 * <li>0 : normal exit</li>
	 * <li>1 : error in configuration</li>
	 * <li>2 : error while running fault localization rankings (running tests)</li>
	 * <li>3 : error while serializing rankings</li> 
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		
		Option testsOption = new Option("T", "tests", true, "tests to run as class names");
		testsOption.setRequired(true);
		testsOption.setArgs(Option.UNLIMITED_VALUES);
		testsOption.setType(String.class);
		
		Option idOption = new Option("d", "id", true, "instrumented class id");
		idOption.setRequired(true);
		idOption.setArgs(1);
		idOption.setType(Long.class);
		
		Option instrumentedClassOption = new Option("i", "instrumentedClass", true, "the instrumented class");
		instrumentedClassOption.setRequired(true);
		instrumentedClassOption.setArgs(1);
		instrumentedClassOption.setType(String.class);
		
		Option formulasOption = new Option("f", "formulas", true, "Spectrum based fault localization formulas");
		formulasOption.setRequired(true);
		formulasOption.setArgs(Option.UNLIMITED_VALUES);
		formulasOption.setType(FORMULA.class);
		
		Option verboseOption = new Option("v", "verbose", false, "enable verbosity");
		verboseOption.setRequired(false);
		
		Option help = new Option("h", "help", false, "print commands");
		help.setRequired(false);
		
		options.addOption(idOption);
		options.addOption(instrumentedClassOption);
		options.addOption(testsOption);
		options.addOption(formulasOption);
		
		
		CommandLineParser parser = new DefaultParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			
			if (cmd.hasOption('h')) {
				System.out.println("FaultLocalization external JUnit runner");
				System.out.println("Version : "+ VERSION);
				System.out.println("Console version");
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("FaultLocalization external JUnit runner", options );
				System.exit(1);
			}
			
			boolean verbose = false;
			
			if (cmd.hasOption(verboseOption.getOpt())) {
				verbose = true;
			}
			
			if (verbose) System.out.println("Validating parameters...");
			
			String[] tclasses = cmd.getOptionValues(testsOption.getOpt());
			if (verbose) System.out.println("Tests classes: "+Arrays.toString(tclasses));
			
			Long id = Long.valueOf(cmd.getOptionValue(idOption.getOpt()));
			
			if (verbose) System.out.println("Instrumented class id : " + id);
			
			String instrumentedClass = cmd.getOptionValue(instrumentedClassOption.getOpt());
			if (verbose) System.out.println("Instrumented class: "+ instrumentedClass); //TODO: this is not validated!
			
			String[] formulas = cmd.getOptionValues(formulasOption.getOpt());
			FORMULA[] fs = new FORMULA[formulas.length];
			int i = 0;
			for (String f : formulas) {
				FORMULA formula = isValidFormula(f);
				if (formula == null) {
					System.err.println(f + " is not a valid FORMULA");
					System.exit(1);
				}
				fs[i] = formula;
				i++;
			}
			if (verbose) System.out.println("Formulas: " + Arrays.toString(formulas));
			
			List<Ranking> rankings = RankingApi.createRankings(instrumentedClass, id, tclasses, fs);
			ObjectOutputStream out = new ObjectOutputStream(System.out);
	        for (Ranking r : rankings) {
	            out.writeObject(r);
	        }
	        out.flush();
		} catch (ParseException e) {
			System.err.println("Incorrect options.  Reason: " + e.getMessage());
			System.err.println(ExceptionUtils.getFullStackTrace(e));
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Error while serializing results");
			System.err.println(ExceptionUtils.getFullStackTrace(e));
			System.exit(3);
		}
	}

	
	private static FORMULA isValidFormula(String f) {
		for (FORMULA formula : FORMULA.values()) {
			if (formula.toString().compareTo(f) == 0) {
				return formula;
			}
		}
		return null;
	}

}
