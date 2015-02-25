package hr.hashcode.hcjdb.controller;

import hr.hashcode.hcjdb.io.ConsoleModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter {

	private enum IntraCommand {
		NONE, LIST_CLASSES, LIST_METHODS, CONDITION
	};

	public static final String[] commands = { "connectors", "run", "threads", "thread", "suspend", "resume", "where", "wherei", "up", "down", "kill", "interrupt", "print", "dump", "eval", "set",
			"locals", "classes", "class", "methods", "fields", "threadgroups", "threadgroup", "stop", "in", "at", "clear", "catch", "ignore", "watch", "unwatch", "trace", "trace", "untrace", "step",
			"stepi", "next", "cont", "list", "use", "exclude", "classpath", "monitor", "unmonitor", "read", "lock", "threadlocks", "pop", "reenter", "redefine", "disablegc", "enablegc", "?", "help",
			"version", "exit", "quit", "!!", "#" };

	public static class InterpreterOutput {
		private boolean sendToProcess = false;
		private String output;

		public InterpreterOutput getValue(boolean sendToProcess, String output) {
			this.sendToProcess = sendToProcess;
			this.output = output;
			return this;
		}

		public InterpreterOutput getValue(boolean sendToProcess) {
			if (sendToProcess)
				throw new IllegalArgumentException("sendToProcess must be false");
			this.sendToProcess = sendToProcess;
			return this;
		}

		public boolean getOutputFlag() {
			return sendToProcess;
		}

		public String getOutput() {
			if (!sendToProcess)
				throw new IllegalArgumentException("sendToProcess must be true");
			return output;
		}
	}

	private static Interpreter instance = null;
	private InterpreterOutput retValue = new InterpreterOutput();
	private IntraCommand intraCommand = IntraCommand.NONE;
	private Pattern classesPattern = Pattern.compile("^([0-9a-zA-Z]+\\.)+[a-zA-Z]+");
	private Pattern methodsPattern = Pattern.compile("^.+\\s(.+)\\(.*\\)");
	private Pattern condBreakpointPattern = Pattern.compile("Breakpoint hit: .+, (.+)\\(.*\\), line=([0-9]+) .*");
	private ConditionBrakepoint conditionBreakpoint = new ConditionBrakepoint();

	public static Interpreter instance() {
		if (instance == null)
			instance = new Interpreter();
		return instance;
	}

	private Interpreter() {
	}

	public InterpreterOutput processInput(String command, ConsoleModule console) {
		if (command.equals(":f")) {
			console.setCompleterFiles();
			return retValue.getValue(false);
		} else if (command.equals(":s")) {
			console.setCompleterStrings();
			return retValue.getValue(false);
		} else if (command.equals(":lc")) {
			intraCommand = IntraCommand.LIST_CLASSES;
			return retValue.getValue(true, "classes");
		} else if (command.startsWith(":m")) {
			intraCommand = IntraCommand.LIST_METHODS;
			String[] splitCommand = command.split(" ");
			if (splitCommand.length != 2)
				return retValue.getValue(false);
			return retValue.getValue(true, "methods " + splitCommand[1]);
		} else if (command.equals(":c")) {
			console.clearWordSet();
			console.setCompleterStrings(Arrays.asList(commands));
			return retValue.getValue(false);
		} else if (command.startsWith(":b")) {
			command = command.substring(2);
			String[] splitCommand = command.split(">>");
			if (splitCommand.length != 2)
				return retValue.getValue(false);
			conditionBreakpoint.addBreakpoint(splitCommand[0].trim(), splitCommand[1]);
			return retValue.getValue(true, "stop at " + splitCommand[0]);
		} else if (command.startsWith("clear")) {
			String[] splitCommand = command.split(" ");
			if (splitCommand.length == 2)
				conditionBreakpoint.removeBreakpoint(splitCommand[1]);
		}

		return retValue.getValue(true, command);
	}

	private void putWordsInCompleter(String output, Pattern pattern, ConsoleModule console, int matchGroup) {
		List<String> words = new ArrayList<String>();
		for (String word : output.split("\n")) {
			Matcher m = pattern.matcher(word);
			if (m.matches())
				words.add(m.group(matchGroup));
		}
		console.setCompleterStrings(words);
	}

	private boolean parseCondition(String output) {
		String[] splitOutput = output.split("\n");
		for (String line : splitOutput) {
			String[] splitLine = line.split("=");
			String last = splitLine[splitLine.length - 1];
			if (last.trim().equals("true"))
				return true;
		}
		return false;
	}

	public boolean processOutput(String output, ConsoleModule console, Controller controller) {

		Matcher breakPoint = condBreakpointPattern.matcher(output);

		if (breakPoint.find()) {
			String methodName = breakPoint.group(1);
			String lineNumber = breakPoint.group(2);
			String condition = conditionBreakpoint.getCondition(methodName, lineNumber);
			if (condition != null) {
				intraCommand = IntraCommand.CONDITION;
				controller.sendCommand("print " + condition);
				return true;
			}
		}

		boolean ret = true;
		switch (intraCommand) {
			case CONDITION:
				boolean condition = parseCondition(output);
				if (condition == false) {
					controller.sendCommand("cont");
					ret = false;
				} else {
					ret = true;
				}
			break;
			case LIST_CLASSES:
				putWordsInCompleter(output, classesPattern, console, 0);
				ret = false;
			break;
			case LIST_METHODS:
				putWordsInCompleter(output, methodsPattern, console, 1);
				ret = false;
			break;
			case NONE:
			break;
			default:
			break;
		}

		intraCommand = IntraCommand.NONE;
		return ret;
	}
}
