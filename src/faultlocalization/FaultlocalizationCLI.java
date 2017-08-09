package faultlocalization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import faultlocalization.api.FaultLocalizationApi;
import faultlocalization.api.MuJavaApi;
import faultlocalization.api.Request;
import faultlocalization.data.Ranking;
import faultlocalization.formulas.SpectrumBasedFormula;
import faultlocalization.formulas.SpectrumBasedFormula.FORMULA;
import faultlocalization.utils.CompilationUtils;
import faultlocalization.utils.FileUtils;

public class FaultlocalizationCLI {
	
	public static final String version = "1.0";
	public static int rankedStatements = 3;
	public static int mutGenLimitMin = 1;
	public static int mutGenLimitMax = 5;
	public static boolean generateGradientVersion = false;
	public static int gradientStep = 2;

	public static void main(String[] args) {
		args = new String[]{
				"-s", "/home/stein/Projects/FaultLocalization/FaultLocalization/faulty_code",
				"-b", "/home/stein/Projects/FaultLocalization/FaultLocalization/bin",
				"-c", "introclass.median.introclass_0cdfa335_003",
				"-j", "median_tests.introclass_0cdfa335_003_Tests",
				"-o", "/home/stein/Desktop/FL",
				"-f", "tarantula", "OCHIAI", "OP2", "BARINEL", "DSTAR",
				"-n", "3",
				"-m", "1", "5",
				"-g", "2"};
		
		Options options = new Options();
		Option srcOption = new Option("s", "src", true, "qualified path to source root folder e.g.: src/ or /Users/ppargento/Documents/workspace/pepe/src/");
		srcOption.setRequired(true);
		srcOption.setType(String.class);
		srcOption.setArgs(1);
		
		Option binOption = new Option("b", "bin", true, "qualified path to binary root folder e.g.: bin/ or /Users/ppargento/Documents/workspace/pepe/bin/");
		srcOption.setRequired(true);
		srcOption.setType(String.class);
		srcOption.setArgs(1);
		
		Option className = new Option("c", "class-name", true, "qualified class name of the class to evaluate e.g.: main.util.Pair");
		className.setRequired(true);
		className.setType(String.class);
		className.setArgs(1);
		
		Option tests = new Option("j", "junit-tests", true, "jUnit tests to use");
		tests.setRequired(true);
		tests.setType(String.class);
		tests.setArgs(Option.UNLIMITED_VALUES);
		
		Option techniques = new Option("f", "techniques", true, "spectrum-based fault localization techniques to be used");
		techniques.setRequired(false);
		techniques.setType(FORMULA.class);
		techniques.setArgs(Option.UNLIMITED_VALUES);
		
		Option output = new Option("o", "output", true, "The folder in which to save the instrumented faulty class file");
		output.setRequired(true);
		output.setType(String.class);
		output.setArgs(1);
		
		Option rankLimit = new Option("n", "rankLimit", true, "How many ranked statements will be taken into account when generating the mutGenLimit versions");
		rankLimit.setRequired(false);
		rankLimit.setType(Integer.class);
		rankLimit.setArgs(1);
		
		Option mglLimit = new Option("m", "mutGenLimitRange", true, "Max value for mutGenLimit comments");
		mglLimit.setRequired(false);
		mglLimit.setType(Integer.class);
		mglLimit.setArgs(2);
		
		Option gradientVersion = new Option("g", "gradientVersion", true, "Generate gradient mutGenLimit version");
		gradientVersion.setRequired(false);
		gradientVersion.setType(Integer.class);
		gradientVersion.setArgs(1);
		
		Option help = new Option("h", "help", false, "print commands");
		help.setRequired(false);
		
		Options miscOptions = new Options();
		miscOptions.addOption(help);
		
		options.addOption(srcOption);
		options.addOption(binOption);
		options.addOption(className);
		options.addOption(tests);
		options.addOption(techniques);
		options.addOption(output);
		options.addOption(rankLimit);
		options.addOption(mglLimit);
		options.addOption(gradientVersion);
		options.addOption(help);
		
		CommandLineParser parser = new DefaultParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			String[] currentCP = CompilationUtils.getCurrentClasspathAsArray();
			
			String[] classpath = new String[currentCP.length + 1];
			
			for (int c = 0; c < currentCP.length; c++) {
				classpath[c] = currentCP[c];
			}
			
			File srcFolder = Paths.get(cmd.getOptionValue(srcOption.getOpt())).toFile();
			
			if (!srcFolder.exists() || !srcFolder.isDirectory()) {
				System.err.println("Source folder doesn't exist or is not a folder : " + srcFolder.toString());
				System.exit(1);
			}
			
			System.out.println("Source folder: " + srcFolder.getAbsolutePath());
			
			File binFolder = Paths.get(cmd.getOptionValue(binOption.getOpt())).toFile();
			
			if (!binFolder.exists() || !binFolder.isDirectory()) {
				System.err.println("Binary folder doesn't exist or is not a folder : " + binFolder.toString());
				System.exit(1);
			}
			
			System.out.println("Binary folder: " + binFolder.getAbsolutePath());
			
			String outputValue = cmd.getOptionValue(output.getOpt());
			File outputFolder = Paths.get(cmd.getOptionValue(output.getOpt())).toFile();
			
			if (outputFolder.isFile()) {
				System.err.println("Output must be a folder : " + outputValue);
				System.exit(1);
			}
			
			if (!outputFolder.exists()) outputFolder.mkdirs();
			
			System.out.println("Output folder: " + outputFolder.getAbsolutePath());
			classpath[classpath.length-1] = outputFolder.getAbsolutePath();
			
			String faultyClassName = cmd.getOptionValue(className.getOpt());
			
			if (!FileUtils.containsClass(srcFolder.getAbsolutePath(), faultyClassName, true)) {
				System.err.println("Class " + faultyClassName + " java file is not found in the source folder");
				System.exit(1);
			}
			
			if (!FileUtils.containsClass(binFolder.getAbsolutePath(), faultyClassName, false)) {
				System.err.println("Class " + faultyClassName + " class file is not found in the binary folder");
				System.exit(1);
			}
			
			System.out.println("Class to analyze: " + faultyClassName);
			
			String[] jtests = cmd.getOptionValues(tests.getOpt());
			
			for (String t : jtests) {
				if (!FileUtils.containsClass(classpath, t, false)) {
					System.err.println("Test " + t + " is not present in classpath");
					System.exit(1);
				}
			}
			
			System.out.println("JUnit tests: " + Arrays.toString(jtests));
			
			FORMULA[] sbFormulas = null;
					
			if (cmd.hasOption(techniques.getOpt())) {
			
				String[] sbTechniques = cmd.getOptionValues(techniques.getOpt());
				sbFormulas = new FORMULA[sbTechniques.length];
				int f = 0;
				for (String sbt : sbTechniques) {
					boolean found = false;
					for (FORMULA sbf : SpectrumBasedFormula.FORMULA.values()) {
						if (sbt.compareToIgnoreCase(sbf.getName()) == 0) {
							found = true;
							sbFormulas[f] = sbf;
							f++;
							break;
						}
					}
					if (!found) {
						System.err.println("Technique " + sbt + " is not valid");
						System.exit(1);
					}
				}
				
			}
			
			if (sbFormulas.length == 0) {
				sbFormulas = SpectrumBasedFormula.FORMULA.values();
			}
			
			System.out.println("Formulas: " + Arrays.toString(sbFormulas));
			
			Random rng = new Random();
			Long id = rng.nextLong();
			System.out.println("ID: " + id.toString());
			Request flRequest = new Request(	faultyClassName,
												srcFolder.getAbsolutePath(),
												binFolder.getAbsolutePath(),
												outputFolder.getAbsolutePath(),
												classpath,
												sbFormulas,
												jtests,
												id
											);
			
			if (cmd.hasOption(rankLimit.getOpt())) {
				Integer rl = Integer.parseInt(cmd.getOptionValue(rankLimit.getOpt()));
				if (rl > 0) {
					rankedStatements = rl;
				} else {
					System.err.println("Max ranked statements to consider must be a positive value " + rl);
					System.exit(1);
				}
			}
			
			if (cmd.hasOption(mglLimit.getOpt())) {
				Integer mgl = Integer.parseInt(cmd.getOptionValue(mglLimit.getOpt()));
				if (mgl > 0) {
					mutGenLimitMax = mgl;
				} else {
					System.err.println("Max mutGenLimit value must be a positive value " + mgl);
					System.exit(1);
				}
			}
			
			if (cmd.hasOption(mglLimit.getOpt())) {
				String[] mglRangeValues = cmd.getOptionValues(mglLimit.getOpt());
				Integer[] mglRange = new Integer[] {Integer.parseInt(mglRangeValues[0]), Integer.parseInt(mglRangeValues[1])};
				if (mglRange[0] > mglRange[1]) {
					System.err.println("Min mutGenLimit is greater than Max mutGenLimit (" + mglRange[0] + "..." + mglRange[1] + "");
				}
				if (mglRange[0] > 0) {
					mutGenLimitMin = mglRange[0];
				} else {
					System.err.println("Min mutGenLimit value must be a positive value " + mglRange[0]);
					System.exit(1);
				}
				if (mglRange[1] > 0) {
					mutGenLimitMax = mglRange[1];
				} else {
					System.err.println("Max mutGenLimit value must be a positive value " + mglRange[1]);
					System.exit(1);
				}
			}
			
			if (cmd.hasOption(gradientVersion.getOpt())) {
				Integer gstep = Integer.parseInt(cmd.getOptionValue(gradientVersion.getOpt()));
				if (gstep > 0) {
					generateGradientVersion = true;
					gradientStep = gstep;
				} else {
					System.err.println("Gradient version step must be a positive value " + gstep);
					System.exit(1);
				}
			}
			
			System.out.println("Fault Localization - version " + version);
			System.out.println("Running Fault Localization with the following arguments");
			System.out.println("--------------------------------------------------------");
			System.out.println("Faulty code source folder         : " + srcFolder.getPath().toString());
			System.out.println("Faulty code binary folder         : " + binFolder.getPath().toString());
			System.out.println("Faulty code classname             : " + faultyClassName);
			System.out.println("jUnit tests                       : ");
			System.out.println(Arrays.toString(jtests));
			System.out.println("Spectrum-Based formulas : ");
			System.out.println(Arrays.toString(sbFormulas));
			System.out.println("Output folder                     : " + outputFolder.getPath().toString());
			System.out.println("Classpath : ");
			System.out.println(Arrays.toString(classpath));
			System.out.println("Max ranked statements to consider : " + rankedStatements);
			System.out.println("Min mutGenLimit value to use      : " + mutGenLimitMax);
			System.out.println("Max mutGenLimit value to use      : " + mutGenLimitMax);
			System.out.println("Generate gradient version         : " + generateGradientVersion);
			if (generateGradientVersion)
				System.out.println("Gradient version step             : " + gradientStep);
			
			//Calculate rankings and output results
			
			List<Ranking> rankings = FaultLocalizationApi.createRankings(flRequest);
			
			if (rankings == null) {
				System.err.println("Failed to generate rankings");
				System.exit(2);
			}
			
			System.out.println("Coverage information");
			System.out.println(rankings.get(0).getCI().toString());
			
			for (Ranking r : rankings) {
				System.out.println("================================");
				System.out.println(r.toString());
				System.out.println("================================");
				System.out.println();
			}
			
			
			for (Ranking r : rankings) {
				if (!MuJavaApi.generateMutGenLimitVersions(r, srcFolder, outputFolder, faultyClassName, rankedStatements, mutGenLimitMin, mutGenLimitMax)) {
					System.err.println("Couldn't generate mutGenLimit versions");
					System.exit(2);
				}
			}
			
			
			
		} catch (ParseException e) {
			System.err.println("Incorrect options.  Reason: " + e.getMessage() );
			return;
		} catch (com.github.javaparser.ParseException e) {
			System.err.println("Error while instrumenting (parsing/modifying AST)");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error while instrumenting (when writing instrumented file)");
			e.printStackTrace();
		}

	}

}
