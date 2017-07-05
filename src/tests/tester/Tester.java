package tests.tester;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public interface Tester<I, O> {

	public O testClassMethod(I input, File classFile, String className, String method) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
	
	public boolean testClassMethod(I input, File classFile, String className, String method, O expectedOutput) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
	
}
