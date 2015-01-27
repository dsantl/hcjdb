package hr.hashcode.hcjdb.callback;

import java.util.EnumMap;

public class Caller<E extends Enum<E>> {

	private EnumMap<E, CallbackDescriptor> callbackMapping;

	private static class CallbackDescriptor {
		private Callback<?, ?> callback;
		private Class<?> inputClass;
		private Class<?> outputClass;

		public <I, O> CallbackDescriptor(Callback<I, O> callback, Class<I> clazzI, Class<O> clazzO) {
			this.callback = callback;
			inputClass = clazzI;
			outputClass = clazzO;
		}

		@SuppressWarnings("unchecked")
		public <I, O> O call(I input, Class<I> clazzI, Class<O> clazzO) {
			if (inputClass.equals(clazzI) && outputClass.equals(clazzO)) {
				return ((Callback<I, O>) callback).callback(input);
			} else
				throw new ClassCastException("Callback and caller have different callback description!");
		}
	}

	public Caller(Class<E> enumClass) {
		callbackMapping = new EnumMap<E, CallbackDescriptor>(enumClass);
	}

	public <I, O> void setCallback(E enumElement, Callback<I, O> callback, Class<I> clazzI, Class<O> clazzO) {
		callbackMapping.put(enumElement, new CallbackDescriptor(callback, clazzI, clazzO));
	}

	public <I, O> O call(E enumElement, I input, Class<I> clazzI, Class<O> clazzO) throws RuntimeException {
		if (callbackMapping.get(enumElement) != null)
			return callbackMapping.get(enumElement).call(input, clazzI, clazzO);
		else
			throw new RuntimeException("Callback is not set!");
	}

}
