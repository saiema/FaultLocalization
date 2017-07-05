package tests.utils.filters;

import java.io.IOException;

public interface Input<I> {
	
	public I getAllInput() throws IOException;
	public I[] getInputAsLines() throws IOException;

}
