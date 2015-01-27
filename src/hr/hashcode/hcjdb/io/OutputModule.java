package hr.hashcode.hcjdb.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class OutputModule {

	private BufferedWriter bufferedWriter = null;

	public void setOutputStream(OutputStream outputStream) {
		bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
	}

	public Boolean sendInputToProcess(String input) {
		if (input == null)
			return false;
		try {
			bufferedWriter.write(input);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}
