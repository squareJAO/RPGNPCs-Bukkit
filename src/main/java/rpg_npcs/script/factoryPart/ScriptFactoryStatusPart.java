package rpg_npcs.script.factoryPart;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rpg_npcs.ParserFactorySet;
import rpg_npcs.logging.Log;
import rpg_npcs.logging.Logged;
import rpg_npcs.prerequisite.PrerequisiteSet;
import rpg_npcs.script.Script;
import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.node.ScriptNode;
import rpg_npcs.script.node.status.ScriptBranchNode;
import rpg_npcs.script.node.status.ScriptClearNode;

public class ScriptFactoryStatusPart extends ScriptFactoryPart {
	private final ParserFactorySet factorySet;

	public ScriptFactoryStatusPart(ParserFactorySet factorySet) {
		super('|', '|');
		
		this.factorySet = factorySet;
	}

	@Override
	protected ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction) {
		ScriptNode newNode = null;
		
		if (instruction == "") {
			newNode = new ScriptClearNode();
			return ScriptFactoryPartData.fromNode(newNode, 10);
		} else {
			Matcher instructionMatcher = Pattern.compile("\\G((?<prerequisites>([^:;]+):([^:;]+)(;([^:;]+):([^:;]+))*)\\?\\s*)?(?<branch>.+)$")
					.matcher(instruction);
			
			if (!instructionMatcher.matches()) {
				return ScriptFactoryPartData.fromError("Invalid branch instruction: " + instruction);
			}
			
			String prerequisitesString = instructionMatcher.group("prerequisites");
			String branchNameString = instructionMatcher.group("branch");
			
			PrerequisiteSet prerequisiteSet = new PrerequisiteSet();
			if (prerequisitesString != null) {
				Logged<PrerequisiteSet> loggedPrerequisiteSet = factorySet.getPrerequisiteFactory().createPrerequisiteSet(prerequisitesString);

				Log log = loggedPrerequisiteSet.getLog();
				if (log.countErrors() > 0) {
					return ScriptFactoryPartData.fromError(log.getErrors().getFormattedString());
				}
				
				prerequisiteSet = loggedPrerequisiteSet.getResult();
			}
			
			// Interpret as node to jump to
			if (!state.doesScriptExist(branchNameString)) {
				return ScriptFactoryPartData.fromError("Unknown script to branch to: " + branchNameString);
			}
			
			Script scriptIfMet = state.getScript(branchNameString);
			newNode = new ScriptBranchNode(scriptIfMet, prerequisiteSet);
		}
		
		return ScriptFactoryPartData.fromNode(newNode);
	}
	
	@Override
	public boolean shouldTrimSpacesBefore() {
		return true;
	}
	
	@Override
	public boolean shouldTrimSpacesAfter() {
		return true;
	}
}
