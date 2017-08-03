package tests.median;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import tests.utils.filters.FileToStringInput;
import tests.utils.filters.median.MedianInputFilter;
import tests.utils.filters.median.MedianOutputFilter;

@RunWith(Parameterized.class)
public class introclass_cd2d9b5b_010_MedianTestsWhitebox {
	
	private Integer[] inputs;
	private Integer output;
	private static final MedianInputFilter mif = new MedianInputFilter();
	private static final MedianOutputFilter mof = new MedianOutputFilter();
	private static final MedianTester mt = new MedianTester();
	
	
	public introclass_cd2d9b5b_010_MedianTestsWhitebox(String inFile, String outFile) throws IOException {
		File iFile = new File(inFile);
		File oFile = new File(outFile);
		FileToStringInput ftsi_in = new FileToStringInput(iFile);
		FileToStringInput ftsi_out = new FileToStringInput(oFile);
		this.inputs = mif.filterElementsFromInput(ftsi_in)[0];
		this.output = mof.filterElementsFromInput(ftsi_out)[0];
	}
	
	@Parameters
    public static Collection<Object[]> data() {
    	List<Object[]> results = new LinkedList<>();
    	results.add(new Object[] {"tests/median/whitebox/1.in", "tests/median/whitebox/1.out"});
    	results.add(new Object[] {"tests/median/whitebox/2.in", "tests/median/whitebox/2.out"});
    	results.add(new Object[] {"tests/median/whitebox/3.in", "tests/median/whitebox/3.out"});
    	results.add(new Object[] {"tests/median/whitebox/4.in", "tests/median/whitebox/4.out"});
    	results.add(new Object[] {"tests/median/whitebox/5.in", "tests/median/whitebox/5.out"});
    	results.add(new Object[] {"tests/median/whitebox/6.in", "tests/median/whitebox/6.out"});
    	
    	results.add(new Object[] {"tests/median/blackbox/1.in", "tests/median/blackbox/1.out"});
    	results.add(new Object[] {"tests/median/blackbox/2.in", "tests/median/blackbox/2.out"});
    	results.add(new Object[] {"tests/median/blackbox/3.in", "tests/median/blackbox/3.out"});
    	results.add(new Object[] {"tests/median/blackbox/4.in", "tests/median/blackbox/4.out"});
    	results.add(new Object[] {"tests/median/blackbox/5.in", "tests/median/blackbox/5.out"});
    	results.add(new Object[] {"tests/median/blackbox/6.in", "tests/median/blackbox/6.out"});
    	
    	return results;
    }

	@Test
	public void test() {
		try {
			Integer output = mt.testClassMethod(inputs, new File("/Users/gaston/Desktop/FL/"), "introclass.median.introclass_cd2d9b5b_010", "median");
			//Integer output = mt.testClassMethod(inputs, new File("/home/stein/Projects/Introclass/IntroclassCustomTesting/bin/oracles/Median.class"), "oracles.Median", "median");
			assertEquals("Expected " + this.output + " got " + output + " instead", this.output, output);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			fail("Exception");
		}
	}

}
