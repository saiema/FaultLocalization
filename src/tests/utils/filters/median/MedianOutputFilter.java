package tests.utils.filters.median;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tests.utils.filters.Filter;
import tests.utils.filters.Input;

public class MedianOutputFilter implements Filter<String, Integer> {

	private Integer[] parsedInputs;
	private Input<String> lastInput;
	private static final Pattern firstPart = Pattern.compile("Please enter 3 numbers separated by spaces \\>", Pattern.CASE_INSENSITIVE);
	private static final Pattern secondPart = Pattern.compile("is the median", Pattern.CASE_INSENSITIVE);
	
	@Override
	public Integer[] filterElementsFromInput(Input<String> input) throws IOException {
		//Output to parse
		//Please enter 3 numbers separated by spaces > N is the median
		
		if (input == null) throw new IllegalArgumentException("Null input");
		if (this.lastInput == null || this.lastInput != input) {
			return filterInputs(input.getInputAsLines());
		} else if (this.lastInput == input) {
			return this.parsedInputs;
		}
		return null;
	}
	
	private Integer[] filterInputs(String[] inputLines) {
		Integer[] filteredInputs = new Integer[inputLines.length];
		int c = 0;
		for (String line : inputLines) {
			Matcher fpm = MedianOutputFilter.firstPart.matcher(line);
			String lineWOFP = fpm.replaceAll("");
			Matcher spm = MedianOutputFilter.secondPart.matcher(lineWOFP);
			String filteredStringValue = spm.replaceAll("");
			filteredInputs[c] = Integer.valueOf(filteredStringValue.trim());
			c++;
		}
		return filteredInputs;
	}

}
