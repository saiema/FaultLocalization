package tests.utils.filters.median;

import java.io.IOException;

import tests.utils.filters.Filter;
import tests.utils.filters.Input;

public class MedianInputFilter implements Filter<String, Integer[]> {
	
	private Integer[][] parsedInputs;
	private Input<String> lastInput;
	

	@Override
	public Integer[][] filterElementsFromInput(Input<String> input) throws IOException {
		if (input == null) throw new IllegalArgumentException("Null input");
		if (this.lastInput == null || this.lastInput != input) {
			return filterInputs(input.getInputAsLines());
		} else if (this.lastInput == input) {
			return this.parsedInputs;
		}
		return null;
	}


	private Integer[][] filterInputs(String[] inputLines) {
		Integer[][] filteredInputs = new Integer[inputLines.length][3];
		int c = 0;
		for (String line : inputLines) {
			String[] values = line.split(" ");
			Integer[] parsedValues = new Integer[3];
			int i = 0;
			for (String v : values) {
				parsedValues[i] = Integer.valueOf(v.trim());
				i++;
			}
			filteredInputs[c] = parsedValues;
			c++;
		}
		return filteredInputs;
	}

}
