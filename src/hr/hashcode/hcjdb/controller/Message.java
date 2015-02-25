package hr.hashcode.hcjdb.controller;

public class Message {

	public enum MessageType {
		CONSOLE_OUTPUT, PROCESS_OUTPUT, STDERR_OUTPUT, PROCESS_EXIT
	};

	private MessageType type;
	private String msg;

	public Message(MessageType type, String msg) {
		this.type = type;
		this.msg = msg;
	}

	public Message(MessageType type) {
		this.type = type;
		this.msg = null;
	}

	public Message setMessage(String msg) {
		this.msg = msg;
		return this;
	}

	public String getMessage() {
		return msg;
	}

	public MessageType getType() {
		return type;
	}
}
