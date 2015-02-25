package hr.hashcode.hcjdb.controller;

import hr.hashcode.hcjdb.callback.Callback;
import hr.hashcode.hcjdb.controller.Interpreter.InterpreterOutput;
import hr.hashcode.hcjdb.controller.Message.MessageType;
import hr.hashcode.hcjdb.io.ConsoleModule;
import hr.hashcode.hcjdb.io.InputModule;
import hr.hashcode.hcjdb.io.OutputModule;

import java.io.IOException;
import java.util.Arrays;

class Controller extends Callback<Message, Void> {

	private static Controller controller = null;

	private InputModule errorReport = new InputModule(MessageType.STDERR_OUTPUT);
	private InputModule inputModule = new InputModule(MessageType.PROCESS_OUTPUT);
	private OutputModule outputModule = new OutputModule();
	private ProcessWrapper processWrapper = ProcessWrapper.instance();
	private ConsoleModule console = new ConsoleModule();
	private boolean outputIsReady = false;
	private boolean errorIsReady = false;
	private boolean exitMessage = false;

	private Controller() {
	}

	public static Controller instance() {
		if (controller == null)
			return (controller = new Controller());
		else
			return controller;
	}

	private boolean init(String[] args) {

		if (!console.ready())
			return false;

		console.setCompleterStrings(Arrays.asList(args));
		console.setCompleterStrings(Arrays.asList(Interpreter.commands));

		inputModule.setProcessOutputCallback(this);
		console.setInputCallback(this);

		try {
			processWrapper.prepareProcess(args);
			processWrapper.runProcess();

			outputModule.setOutputStream(processWrapper.getOutputStream());
			errorReport.setInputStream(processWrapper.getErrorStream());
			inputModule.setInputStream(processWrapper.getInputStream());
		} catch (RuntimeException e) {
			// TODO logger
			return false;
		} catch (IOException e) {
			// TODO logger
			return false;
		}

		Thread inputThread = new Thread(inputModule);
		inputThread.start();

		Thread errorReportThread = new Thread(errorReport);
		errorReportThread.start();

		Thread consoleThread = new Thread(console);
		consoleThread.start();

		return true;
	}

	private void emptyOutputToScreen() {
		Character inputChar;
		StringBuilder builder = new StringBuilder();

		while ((inputChar = inputModule.getInputChar()) != null) {
			builder.append(inputChar);
		}

		String output = builder.toString();

		while (output.startsWith("> ")) {
			output = output.substring(2);
		}

		boolean toTheScreen = Interpreter.instance().processOutput(output, console, this);

		if (toTheScreen) {
			System.out.print(output);
		}

		outputIsReady = false;
	}

	private void emptyErrorToScreen() {
		Character inputChar;
		StringBuilder builder = new StringBuilder();

		while ((inputChar = errorReport.getInputChar()) != null) {
			builder.append(inputChar);
		}

		String output = builder.toString();

		System.err.println("\nERROR:");
		System.err.println(output);

		errorIsReady = false;
	}

	public void start(String[] args) {
		init(args);
		while (!exitMessage) {
			if (outputIsReady)
				emptyOutputToScreen();
			if (errorIsReady)
				emptyErrorToScreen();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// Log warning
			}
		}

		outputModule.sendInputToProcess("exit");
		processWrapper.destroy();
		System.out.println("hcjdb - exit");
	}

	@Override
	public Void callback(Message input) {
		switch (input.getType()) {
			case PROCESS_OUTPUT:
				outputIsReady = true;
			break;
			case PROCESS_EXIT:
				exitMessage = true;
			break;
			case CONSOLE_OUTPUT:
				InterpreterOutput interOut = Interpreter.instance().processInput(input.getMessage(), console);
				if (interOut.getOutputFlag())
					outputModule.sendInputToProcess(interOut.getOutput());
			break;
			case STDERR_OUTPUT:
				errorIsReady = true;
			break;
			default:
			// Log error
			break;
		}
		return null;
	}

	public void sendCommand(String input) {
		outputModule.sendInputToProcess(input);
	}
}
