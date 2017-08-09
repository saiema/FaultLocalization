package faultlocalization.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang.exception.ExceptionUtils;

public class CompilationUtils {
	
	
	public static CompilationResult compile(String path, List<String> extraPaths) {
		System.out.println("Compiling with classpath: " + getCurrentClasspath());
		File fileToCompile = new File(path);
		if (!fileToCompile.exists() || !fileToCompile.isFile() || !fileToCompile.getName().endsWith(".java")) {
			return new CompilationResult(new Exception("Error in file : " + fileToCompile.getAbsolutePath()));
		}
		File[] files = new File[]{fileToCompile};
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));
		boolean success = false;
		Exception compilationUnexpectedError = null;
		try {
			success = compiler.getTask(null, fileManager, diagnostics, Arrays.asList(new String[] {
					"-classpath",
					StringUtils.listToString(extraPaths, File.pathSeparator)+File.pathSeparator+getCurrentClasspath()
					}
			), null, compilationUnit).call();
		} catch (RuntimeException e) {
			System.err.println(ExceptionUtils.getFullStackTrace(e));
			compilationUnexpectedError = e;
		} catch (Exception e) {
			System.err.println(ExceptionUtils.getFullStackTrace(e));
			compilationUnexpectedError = e;
		}
		String compilationErrorOutput = "";
		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
			String kind = diagnostic.getKind()==null?"N/A":diagnostic.getKind().toString();
			long lineNumber = diagnostic.getLineNumber();
			String source = diagnostic.getSource()==null?"N/A":diagnostic.getSource().toUri().toString();
	        String msg = diagnostic.getMessage(null)==null?"N/A":diagnostic.getMessage(null);
			compilationErrorOutput += String.format("%s on line %d in %s%n%s%n",
	        										kind,
								                    lineNumber,
								                    source,
								                    msg);
		}
		CompilationResult cresult = null;
		if (compilationUnexpectedError != null) {
			cresult = new CompilationResult(compilationUnexpectedError);
		} else if (success) {
			cresult = new CompilationResult(compilationErrorOutput);
		} else {
			cresult = new CompilationResult(new Exception(compilationErrorOutput));
		}
		return cresult;
	}
	
	public static String getCurrentClasspath() {
		String classpath = System.getProperty("java.class.path");
		return classpath;
	}
	
	public static String[] getCurrentClasspathAsArray() {
		return getCurrentClasspath().split(File.pathSeparator);
	}
}
