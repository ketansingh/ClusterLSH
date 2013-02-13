package edu.usc.pgroup.floe.api.state;

public class StateObjectImpl implements StateObject {
	private Object object;

	public StateObjectImpl(Object state) {
		this.object = state;
	}

	public void set(Object o)
	{
		object = o;
	}
	
	@Override
	public Object get() {
		return object;
	}

}
