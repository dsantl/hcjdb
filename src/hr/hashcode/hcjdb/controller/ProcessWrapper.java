package hr.hashcode.hcjdb.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ProcessWrapper {

	public static ProcessWrapper instance = null;

	private static final String jdbName = "jdb";

	private Process process = null;
	private ProcessBuilder processBuilder = null;

	public void prepareProcess(String[] args) {
		int commandLen = args.length + 1;
		String[] commandArgs = new String[args.length + 1];
		commandArgs[0] = jdbName;
		for (int i = 1; i < commandLen; ++i) {
			commandArgs[i] = args[i - 1];
		}
		processBuilder = new ProcessBuilder(commandArgs);
	}

	public void runProcess() throws IOException {
		if (processBuilder == null)
			throw new RuntimeException("Proces is not prepared!");
		process = processBuilder.start();
	}

	public static ProcessWrapper instance() {
		if (instance == null)
			return (instance = new ProcessWrapper());
		else
			return instance;
	}

	public OutputStream getOutputStream() {
		if (process == null)
			throw new RuntimeException("Proces is not started!");
		return process.getOutputStream();
	}

	public InputStream getInputStream() {
		if (process == null)
			throw new RuntimeException("Proces is not started!");
		return process.getInputStream();
	}

	public InputStream getErrorStream() {
		if (process == null)
			throw new RuntimeException("Proces is not started!");
		return process.getErrorStream();
	}

	public void destroy() {
		process.destroy();
	}
}
