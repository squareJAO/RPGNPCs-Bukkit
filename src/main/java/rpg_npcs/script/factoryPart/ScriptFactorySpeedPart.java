package rpg_npcs.script.factoryPart;

import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;

public class ScriptFactorySpeedPart extends ScriptFactoryPart {
	public ScriptFactorySpeedPart() {
		super('{', '}');
	}

	@Override
	protected ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction) {
		// Parse text to double
		double givenSpeed = 0;
		try {
			givenSpeed = Double.parseDouble(instruction);
		} catch (NumberFormatException e) {
			return ScriptFactoryPartData.fromError("Given delay '" + instruction + "' is not a valid integer");
		}
		
		if (givenSpeed <= 0) {
			return ScriptFactoryPartData.fromError("Given speed " + givenSpeed + " cannot be negative");
		}
		
		// Change speed for future speech nodes
		state.TextSpeed = givenSpeed;
		
		return ScriptFactoryPartData.fromNothing();
	}

}
