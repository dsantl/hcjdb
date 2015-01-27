package hr.hashcode.hcjdb.io;

import hr.hashcode.hcjdb.callback.Callback;
import hr.hashcode.hcjdb.callback.Caller;
import hr.hashcode.hcjdb.controller.Message;
import hr.hashcode.hcjdb.controller.Message.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

public class InputModule implements Runnable {

	private enum ProcessOutputType {
		NEW_OUTPUT
	};

	private Caller<ProcessOutputType> caller = new Caller<ProcessOutputType>(ProcessOutputType.class);
	private InputStreamReader inputStreamReader = null;
	private BufferedReader bufferedReader = null;

	private Queue<Character> inputQueue = new LinkedList<Character>();

	public void setInputStream(InputStream inputStream) {

		if (inputStream == null)
			throw new IllegalAccessError("inputStream can not be null!");

		inputStreamReader = new InputStreamReader(inputStream);
		bufferedReader = new BufferedReader(inputStreamReader);
	}

	private void putInQueue(int input) {
		synchronized (this.inputQueue) {
			inputQueue.add((char) input);
			caller.call(ProcessOutputType.NEW_OUTPUT, new Message(MessageType.PROCESS_OUTPUT), Message.class, Void.class);
		}
	}

	public Character getInputChar() {
		synchronized (this.inputQueue) {
			return this.inputQueue.poll();
		}
	}

	@Override
	public void run() {

		if (bufferedReader == null)
			throw new RuntimeException("Input stream is not set!");

		int input;
		while (true) {
			try {
				input = bufferedReader.read();
			} catch (IOException e) {
				// sendError;
				// TODO logger
				break;
			}
			if (input == -1)
				break;
			putInQueue(input);
		}

		System.out.println("End of input thread");
	}

	public void setProcessOutputCallback(Callback<Message, Void> callback) {
		caller.setCallback(ProcessOutputType.NEW_OUTPUT, callback, Message.class, Void.class);
	}
}
