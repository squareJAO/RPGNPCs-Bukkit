package rpg_npcs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import rpg_npcs.script.ScriptFactory;
import rpg_npcs.script.factoryPart.ScriptFactoryCommandPart;
import rpg_npcs.script.factoryPart.ScriptFactoryPart;
import rpg_npcs.script.factoryPart.ScriptFactoryPausePart;
import rpg_npcs.script.factoryPart.ScriptFactoryQuestionPart;
import rpg_npcs.script.factoryPart.ScriptFactorySpeedPart;
import rpg_npcs.script.factoryPart.ScriptFactoryStatePart;
import rpg_npcs.trigger.Trigger;

public class RPGNPCsPlugin extends JavaPlugin {
	public Map<String, Trigger> triggers = new HashMap<String, Trigger>();
	public Map<String, Role> roles = new HashMap<String, Role>();

	protected ScriptFactory scriptFactory;
	
	final Set<RpgTrait> npcs = new HashSet<RpgTrait>();
	
	private Set<ScriptFactoryPart> parts = new HashSet<ScriptFactoryPart>();
	
	@Override
	public void onEnable(){
		// Check dependencies
		String[] dependantStrings = {"Citizens", "HolographicDisplays"};
		for (String dependantString : dependantStrings) {
			if(!getServer().getPluginManager().isPluginEnabled(dependantString)) {
				// If the plugin isn't in the server let the user know and quit
				getLogger().log(Level.SEVERE, dependantString + " not found or not enabled");
				getServer().getPluginManager().disablePlugin(this);	
				return;
			}
		}
		
		// Add commands
		CommandReloadScripts commandReloadConversations = new CommandReloadScripts(this);
		getCommand("reloadRPGNPCs").setTabCompleter(commandReloadConversations);
		getCommand("reloadRPGNPCs").setExecutor(commandReloadConversations);
		
		// Add default factory parts
		addFactoryPart(new ScriptFactoryPausePart());
		addFactoryPart(new ScriptFactorySpeedPart());
		addFactoryPart(new ScriptFactoryCommandPart());
		addFactoryPart(new ScriptFactoryQuestionPart());
		addFactoryPart(new ScriptFactoryStatePart());
		buildScriptFactory();
		
		// Load all conversations
		ParseLog log = reloadData();
		getLogger().info(log.getFormattedString());
		
		// Add all custom traits
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(RpgTrait.class).withName("Rpgnpc"));
		
		// Check config is on disk
		this.getConfig();
		this.saveConfig();
	}
	
	public boolean hasPlaceholderAPI() {
		return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
	}
	
	public void addFactoryPart(ScriptFactoryPart part) {
		parts.add(part);
	}
	
	public void buildScriptFactory() {
		ScriptFactoryPart[] partsArray = parts.toArray(new ScriptFactoryPart[parts.size()]);
		scriptFactory = new ScriptFactory(this, partsArray, 0.8, 20, "�7");
	}

	public ParseLog reloadData() {
		// Unregister old trigger listeners
		for (Trigger trigger : triggers.values()) {
			HandlerList.unregisterAll(trigger);
		}
		
		// Unregister npcs
		for (RpgTrait npc : npcs) {
			unbindNpcTriggers(npc);
		}
		
		// Stop Roles
		
		
		// Set new data
		ConfigParser.ConfigResult result = ConfigParser.reloadConfig(scriptFactory, getConfig());
		triggers = result.triggerMap;
		roles = result.rolesMap;
		
		// Register trigger listeners
		for (Trigger trigger : triggers.values()) {
			Bukkit.getPluginManager().registerEvents(trigger, this);
		}
		
		// Register npcs
		for (RpgTrait npc : npcs) {
			bindNpcTriggers(npc);
		}
		
		return result.log;
	}
	
	public void registerRPGNPC(RpgTrait npc) {
		npcs.add(npc);
		bindNpcTriggers(npc);
	}
	
	public void unregisterRPGNPC(RpgTrait npc) {
		npcs.remove(npc);
		unbindNpcTriggers(npc);
	}
	
	private void bindNpcTriggers(RpgTrait npc) {
		String npcName = npc.getNPC().getName();
		if (roles.containsKey(npcName)) {
			roles.get(npcName).registerNpc(npc);
		}
	}
	
	private void unbindNpcTriggers(RpgTrait npc) {
		String npcName = npc.getNPC().getName();
		if (roles.containsKey(npcName)) {
			roles.get(npcName).unregisterNpc(npc);
		}
	}
}