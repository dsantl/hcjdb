package hr.hashcode.hcjdb.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jline.console.completer.Completer;

class CommandCompleter implements Completer {

	private List<String> words;

	public CommandCompleter(List<String> candidates) {
		words = new ArrayList<String>(candidates);
		Collections.sort(words);
	}

	@Override
	public int complete(String buffer, int cursor, List<CharSequence> candidates) {

		if (buffer == null || buffer.length() == 0 || buffer.endsWith(" ")) {
			candidates.addAll(words);
			return cursor;
		}

		String[] splitBuffer = buffer.split(" ");

		if (splitBuffer.length > 0)
			buffer = splitBuffer[splitBuffer.length - 1];

		for (String word : words) {
			if (word.startsWith(buffer))
				candidates.add(word);
		}

		return cursor - buffer.length();
	}
}
