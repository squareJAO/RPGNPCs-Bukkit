package rpg_npcs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import rpg_npcs.prerequisite.PrerequisiteFactory;
import rpg_npcs.script.ScriptFactory;
import rpg_npcs.script.factoryPart.ScriptFactoryPart;
import rpg_npcs.state.StateFactory;
import rpg_npcs.state.SupportedStateType;
import rpg_npcs.state.SupportedStateTypeRecords;
import rpg_npcs.trigger.TriggerFactory;

public class ParserFactorySet {
	private ScriptFactory scriptFactory;
	private StateFactory stateFactory;
	private TriggerFactory triggerFactory;
	private PrerequisiteFactory prerequisiteFactory;
	private ConfigParser configParser;
	
	// Data for building above factories
	private Map<String, ScriptFactoryPart> scriptParts;
	private SupportedStateTypeRecords supportedStateTypeRecords;
	private double defaultSpeed;
	private int charactersPerWrap;
	private String defaultLineStartString;
	
	public ParserFactorySet() {
		configParser = new ConfigParser(this);
		
		scriptParts = new HashMap<String, ScriptFactoryPart>();
		defaultSpeed = 0.7;
		charactersPerWrap = 15;
		defaultLineStartString = "";
		supportedStateTypeRecords = new SupportedStateTypeRecords();
		
		rebuild();
	}
	
	/**
	 * Rebuilds all of the factories with the data contained in this. This should be called after values have been changed
	 */
	public final void rebuild() {
		Collection<ScriptFactoryPart> scriptFactoryParts = scriptParts.values();
		scriptFactory = new ScriptFactory(scriptFactoryParts, defaultSpeed, charactersPerWrap, defaultLineStartString);
		
		stateFactory = new StateFactory(supportedStateTypeRecords);
		
		triggerFactory = new TriggerFactory();
		
		prerequisiteFactory = new PrerequisiteFactory(this);
	}
	
	public final void addScriptFactoryPart(String name, ScriptFactoryPart part) {
		if (scriptParts.containsKey(name)) {
			Bukkit.getLogger().log(Level.SEVERE, "Factory part name '" + name + "' is already taken!");
			return;
		}
		
		scriptParts.put(name, part);
	}
	
	public final ScriptFactoryPart getScriptFactoryPart(String name) {
		return scriptParts.get(name);
	}
	
	public final void addSupportedStateType(SupportedStateType<?> stateType) {
		supportedStateTypeRecords.addSupportedType(stateType);
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
}
