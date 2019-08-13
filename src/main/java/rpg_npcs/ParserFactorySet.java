package rpg_npcs;

import java.util.regex.Pattern;

import rpg_npcs.prerequisite.Prerequisite;
import rpg_npcs.prerequisite.PrerequisiteFactory;
import rpg_npcs.script.ScriptFactory;
import rpg_npcs.script.factoryPart.ScriptFactoryPart;
import rpg_npcs.state.StateFactory;
import rpg_npcs.state.StateScope;
import rpg_npcs.state.SupportedStateScopeRecords;
import rpg_npcs.state.StateType;
import rpg_npcs.state.SupportedStateTypeRecords;
import rpg_npcs.trigger.Trigger;
import rpg_npcs.trigger.TriggerFactory;

public class ParserFactorySet {
	private ScriptFactory scriptFactory;
	private StateFactory stateFactory;
	private TriggerFactory triggerFactory;
	private PrerequisiteFactory prerequisiteFactory;
	private ConfigParser configParser;
	
	// Data for building above factories
	private final SupportedStateTypeRecords supportedStateTypeRecords;
	private final SupportedStateScopeRecords supportedStateScopeRecords;
	private double defaultSpeed;
	private int charactersPerWrap;
	private String defaultLineStartString;
	
	public ParserFactorySet(int defaultEventPriority) {
		configParser = new ConfigParser(this, defaultEventPriority);
		
		defaultSpeed = 0.7;
		charactersPerWrap = 15;
		defaultLineStartString = "";
		
		supportedStateTypeRecords = new SupportedStateTypeRecords();
		supportedStateScopeRecords = new SupportedStateScopeRecords();
		stateFactory = new StateFactory(supportedStateTypeRecords, supportedStateScopeRecords);
		scriptFactory = new ScriptFactory(defaultSpeed, charactersPerWrap, defaultLineStartString);
		
		triggerFactory = new TriggerFactory();
		prerequisiteFactory = new PrerequisiteFactory();
	}
	
	public final void addScriptFactoryPart(ScriptFactoryPart part) {
		scriptFactory.addPart(part);
	}
	
	public final void addSupportedStateType(StateType<?> stateType) {
		supportedStateTypeRecords.addSupportedType(stateType);
	}
	
	public final void addSupportedStateScope(StateScope scope) {
		supportedStateScopeRecords.addSupportedType(scope);
	}
	
	public void addSupportedPrerequisite(String prerequisiteKeyword, Class<? extends Prerequisite> prerequisite){
		prerequisiteFactory.addSupportedType(prerequisiteKeyword, prerequisite);
	}

	public void addSupportedTrigger(String string, Class<? extends Trigger> triggerClass) {
		triggerFactory.addTriggerClass(Pattern.compile(string), triggerClass);
	}
	
	public final ScriptFactory getScriptFactory() {
		return scriptFactory;
	}
	
	public final StateFactory getStateFactory() {
		return stateFactory;
	}
	
	public final TriggerFactory getTriggerFactory() {
		return triggerFactory;
	}
	
	public final PrerequisiteFactory getPrerequisiteFactory() {
		return prerequisiteFactory;
	}
	
	public final ConfigParser getConfigParser() {
		return configParser;
	}
	
	public final SupportedStateTypeRecords getSupportedStateTypeRecords() {
		return supportedStateTypeRecords;
	}

	public final void setDefaultSpeed(double defaultSpeed) {
		this.defaultSpeed = defaultSpeed;
	}

	public final void setCharactersPerWrap(int charactersPerWrap) {
		this.charactersPerWrap = charactersPerWrap;
	}

	public final void setDefaultLineStartString(String defaultLineStartString) {
		this.defaultLineStartString = defaultLineStartString;
	}

	public void setDefaultTriggerPriority(int defaultEventPriority) {
		configParser.setDefaultTriggerPriority(defaultEventPriority);
	}
}
