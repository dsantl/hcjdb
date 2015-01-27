package hr.hashcode.hcjdb.callback;

public abstract class Callback<I, O> {

	public abstract O callback(I input);

	public final Callback<I, O> getCallback() {
		return this;
	}

}
