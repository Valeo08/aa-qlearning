package main.qlearning;

import java.io.Serializable;

public class State implements Serializable {
	
	// Implements Serializable
	private static final long serialVersionUID = -542569607199961313L;
	
	private String name;
	
	public State(final String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		
		if (o instanceof State)
			return this.name.equals(((State) o).name);
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return "State: " + name;
	}
	
}
