package hr.hashcode.hcjdb.controller;

import java.util.HashMap;
import java.util.Map;

public class ConditionBrakepoint {

	private Map<String, String> classLineLineCondition = new HashMap<String, String>();

	public void addBreakpoint(String classLine, String condition) {
		classLineLineCondition.put(classLine, condition);
	}

	public void removeBreakpoint(String methodLine) {
		if (classLineLineCondition.containsKey(methodLine))
			classLineLineCondition.remove(methodLine);
	}

	public String getCondition(String methodName, String lineNumber) {

		int lastDot = methodName.lastIndexOf('.');

		if (lastDot == -1)
			return null;

		String key = methodName.substring(0, lastDot) + ":" + lineNumber;

		if (classLineLineCondition.containsKey(key))
			return classLineLineCondition.get(key);

		return null;
	}
}
