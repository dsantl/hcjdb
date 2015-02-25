package hr.hashcode.hcjdb.io;

import hr.hashcode.hcjdb.callback.Callback;
import hr.hashcode.hcjdb.callback.Caller;
import hr.hashcode.hcjdb.controller.Message;
import hr.hashcode.hcjdb.controller.Message.MessageType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;

public class ConsoleModule implements Runnable {

	private enum SendType {
		SEND_INPUT
	};

	private ConsoleReader console = null;
	private Caller<SendType> caller = new Caller<SendType>(SendType.class);
	private Set<String> allWords = new HashSet<String>();

	private final Message consoleOutput;
	private final Message processExit;

	public ConsoleModule() {
		consoleOutput = new Message(MessageType.CONSOLE_OUTPUT);
		processExit = new Message(MessageType.PROCESS_EXIT);
		try {
			this.console = new ConsoleReader();
			this.console.setPrompt("> ");
		} catch (IOException e) {
			// TODO log error
		}
	}

	public void setInputCallback(Callback<Message, Void> callback) {
		caller.setCallback(SendType.SEND_INPUT, callback, Message.class, Void.class);
	}

	public void clearWordSet() {
		clearCompleters();
		allWords.clear();
		List<String> wordList = new ArrayList<String>(allWords);
		console.addCompleter(new CommandCompleter(wordList));
	}

	private void clearCompleters() {
		for (Completer completer : console.getCompleters())
			console.removeCompleter(completer);
	}

	public void setCompleterStrings() {
		clearCompleters();
		List<String> wordList = new ArrayList<String>(allWords);
		console.addCompleter(new CommandCompleter(wordList));
	}

	public void setCompleterStrings(List<String> words) {
		for (String word : words)
			allWords.add(word);
		setCompleterStrings();
	}

	public void setCompleterFiles() {
		clearCompleters();
		console.addCompleter(new FileNameCompleter());
	}

	@Override
	public void run() {

		String line = "";
		while (true) {
			try {
				line = console.readLine();
			} catch (IOException e) {
				System.err.println("Console input error: " + e.getMessage());
				break;
			}
			if (line == null || line.equals("exit") || line.equals("quit"))
				break;
			caller.call(SendType.SEND_INPUT, consoleOutput.setMessage(line), Message.class, Void.class);
		}
		caller.call(SendType.SEND_INPUT, processExit, Message.class, Void.class);
	}

	public void putOnScreen(String s) {
		try {
			console.print(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean ready() {
		if (console == null)
			return false;
		else
			return true;
	}

}
