package tests.utils.filters;

import java.io.IOException;

public interface Filter<I, O> {
	
	public O[] filterElementsFromInput(Input<I> input) throws IOException;

}
