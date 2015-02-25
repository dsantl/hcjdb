package hr.hashcode.hcjdb.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

class ProcessWrapper {

	public static ProcessWrapper instance = null;

	private static final String jdbName = "jdb";

	private Process process = null;
	private ProcessBuilder processBuilder = null;

	public void prepareProcess(String[] args) {

		int commandLen = args.length;
		List<String> commandArgs = new ArrayList<String>();
		commandArgs.add(jdbName);

		for (int i = 0; i < commandLen; ++i) {
			if (args[i].startsWith("--" + jdbName)) {
				String[] pathJdb = args[i].split("=");
				System.out.println(pathJdb[0]);
				System.out.println(pathJdb[1]);
				if (pathJdb.length == 2) {
					commandArgs.set(0, pathJdb[1]);
				}
				continue;
			}
			commandArgs.add(args[i]);
		}
		for (int i = 0; i < commandArgs.size(); ++i)
			System.out.println(commandArgs.get(i));
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
