package faultlocalization.utils;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.exception.ExceptionUtils;

public class FileUtils {
	
	public static enum CHECK {
		IS_FILE,
		IS_DIRECTORY,
		IS_CLASS,
		IS_JAVA,
		EXISTS,
		EXTENSION;
		
		private String value = null;
		
		public void setValue(String v) {
			value = v;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public static boolean checkPathProperties(String path, CHECK... properties) {
		if (path == null) throw new IllegalArgumentException("null path to check");
		if (properties == null) throw new IllegalArgumentException("null properties");
		if (properties.length == 0) throw new IllegalArgumentException("empty properties");
		File f = new File(path);
		for (CHECK p : properties) {
			if (!checkProp(f,p)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean checkProp(File f, CHECK p) {
		switch(p) {
			case IS_FILE: return f.isFile();
			case IS_DIRECTORY: return f.isDirectory();
			case IS_CLASS: return f.getAbsolutePath().endsWith(".class");
			case IS_JAVA: return f.getAbsolutePath().endsWith(".java");
			case EXISTS: return f.exists();
			case EXTENSION: {
				String extension = p.getValue();
				if (extension == null) throw new IllegalArgumentException("EXTENSION propertie without an extension value");
				return f.getAbsolutePath().endsWith("."+extension);
			}
		}
		return false;
	}
	
	public static boolean createNewFile(File f, boolean createParentDirectories) throws IOException {
		if (checkProp(f, CHECK.EXISTS)) {
			return false;
		}
		if (createParentDirectories) {
			if (!f.getParentFile().mkdirs()) return false;
		}
		if (checkProp(f, CHECK.IS_FILE)) {
			return f.createNewFile();
		}
		if (checkProp(f, CHECK.IS_DIRECTORY)) {
			return f.mkdir();
		}
		return false;
	}
	
	public static boolean containsClass(String p, String clazz, boolean asJava) {
		File f = new File(p);
		if (!checkProp(f, CHECK.EXISTS)) return false;
		if (!checkProp(f, CHECK.IS_DIRECTORY)) return false;
		String classAsPath = classToPath(clazz, asJava);
		CHECK jarProp = CHECK.EXTENSION;
		jarProp.setValue("jar");
		if (checkProp(f, jarProp)) {
			try {
				JarFile jar = new JarFile(new File(p));
				return existsInJar(jar, classAsPath);
			} catch (IOException e) {
				System.err.println(ExceptionUtils.getFullStackTrace(e));
				return false;
			}
		}
		File fileToCheck = new File(f, classAsPath);
		return checkProp(fileToCheck, CHECK.EXISTS) && checkProp(fileToCheck, asJava?CHECK.IS_JAVA:CHECK.IS_CLASS); 
	}
	
	public static boolean containsClass(String[] classpath, String clazz, boolean asJava) {
		for (String p : classpath) {
			if (containsClass(p, clazz, asJava)) return true;
		}
		return false;
	}
	
	private static boolean existsInJar(JarFile jarfile, String path) {
		ZipEntry entry = jarfile.getEntry(path);
		if (entry == null) return false;
		return !entry.isDirectory();
	}
	
	
	public static String classToPath(String clazz, boolean asJava) {
		String asPath = clazz.replaceAll("\\.", File.separator);
		return asPath + (asJava?".java":".class");
	}
	
	public static String getClassRootFolder(String fullpath, String clazz) {
		String pathWOextension = fullpath.endsWith(".class")?fullpath.replace("\\.class", ""):(fullpath.endsWith(".java")?fullpath.replace("\\.java", ""):null);
		if (pathWOextension == null) return null;
		String clazzToPath = clazz.replaceAll("\\.", File.separator);
		return pathWOextension.replace(clazzToPath, "");
	}
	
	public static boolean deactivateFile(String path) {
		File f = new File(path);
		if (f.exists() && f.isFile()) {
			return f.renameTo(new File(f.getAbsolutePath()+".bak"));
		} else {
			return false;
		}
	}
	
	public static boolean reactivateFile(String path) {
		File f = new File(path+".bak");
		if (f.exists() && f.isFile()) {
			return f.renameTo(new File(f.getAbsolutePath().replace(".bak", "")));
		} else {
			return false;
		}
	}

}
