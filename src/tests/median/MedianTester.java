package tests.median;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import tests.tester.Tester;
import utils.loader.Reloader;

public class MedianTester implements Tester<Integer[], Integer> {
	
	private Reloader reloader;
	
	public MedianTester() {
		List<String> classPath = new LinkedList<>();
		classPath.add("src/");
		classPath.add("bin/");
		classPath.add("tests/");
		this.reloader = new Reloader(classPath, Thread.currentThread().getContextClassLoader());
	}

	@Override
	public Integer testClassMethod(Integer[] input, File classFile, String className, String method) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		checkFile(classFile);
		this.reloader.setSpecificClassPath(className, classFile.getAbsolutePath() + "/");
		Class<?> medianClass = this.reloader.rloadClass(className, true);
		Method[] methods = medianClass.getDeclaredMethods();
		Method mtr = null;
		for (Method m : methods) {
			if (m.getName().compareToIgnoreCase(method) == 0) {
				mtr = m;
			}
		}
		if (mtr == null) throw new IllegalArgumentException("Class " + className + " doesn't declare method " + method);
		Object instance = Modifier.isFinal(mtr.getModifiers())?null:medianClass.newInstance();
		return (Integer) mtr.invoke(instance, (Object[]) input);
	}

	@Override
	public boolean testClassMethod(Integer[] input, File classFile, String className, String method, Integer expectedOutput) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return testClassMethod(input, classFile, className, method) == expectedOutput;
	}
	
	private void checkFile(File f) {
		if (f == null) throw new IllegalArgumentException("Null file");
		if (!f.exists()) throw new IllegalArgumentException("File " + f.getAbsolutePath() + " doesn't exist");
		if (!f.isDirectory()) throw new IllegalArgumentException("File " + f.getAbsolutePath() + " is not a directory");
		if (!f.canRead()) throw new IllegalArgumentException("File " + f.getAbsolutePath() + " no read access");
		//if (!f.getAbsolutePath().endsWith(".class")) throw new IllegalArgumentException("File " + f.getAbsolutePath() + " is not a class file");
	}

}
