package rpg_npcs.logging;

public class Logged <T> {
	private final T resultT;
	private final Log log;
	
	public Logged(T resultT, Log log) {
		super();
		this.resultT = resultT;
		this.log = log;
	}

	public final T getResult() {
		return resultT;
	}

	public final Log getLog() {
		return log;
	}
}
