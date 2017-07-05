package tests.utils.filters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class FileToStringInput implements Input<String> {
	final private File f;
	transient private List<String> parsedInput;
	transient private String allInput;
	
	
	public FileToStringInput(File f) {
		checkFile(f);
		this.f = f;
	}
	
	
	private void checkFile(File f) {
		if (f == null) throw new IllegalArgumentException("Null file");
		if (!f.exists()) throw new IllegalArgumentException("File " + f.getAbsolutePath() + " doesn't exist");
		if (f.isDirectory()) throw new IllegalArgumentException("File " + f.getAbsolutePath() + " is a directory");
		if (!f.canRead()) throw new IllegalArgumentException("File " + f.getAbsolutePath() + " no read access");
	}


	@Override
	public String getAllInput() throws IOException {
		if (this.parsedInput != null) return this.allInput;
		this.parsedInput = new LinkedList<>();
		this.allInput = "";
		try (Stream<String> stream = Files.lines(this.f.toPath())) {
	        stream.forEach(l -> {this.allInput += l; this.parsedInput.add(l);});
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
        this.allInput.trim();
        return this.allInput;
	}


	@Override
	public String[] getInputAsLines() throws IOException {
		if (this.parsedInput == null) getAllInput();
		return this.parsedInput.toArray(new String[this.parsedInput.size()]);
	}
	
	
}
