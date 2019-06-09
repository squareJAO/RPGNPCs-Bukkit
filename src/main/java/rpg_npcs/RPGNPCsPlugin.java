package rpg_npcs;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import rpg_npcs.role.Role;
import rpg_npcs.script.ScriptFactory;
import rpg_npcs.script.factoryPart.ScriptFactoryCommandPart;
import rpg_npcs.script.factoryPart.ScriptFactoryPart;
import rpg_npcs.script.factoryPart.ScriptFactoryPausePart;
import rpg_npcs.script.factoryPart.ScriptFactoryQuestionPart;
import rpg_npcs.script.factoryPart.ScriptFactorySpeedPart;
import rpg_npcs.script.factoryPart.ScriptFactoryStatePart;

public class RPGNPCsPlugin extends JavaPlugin {
	public Map<String, Role> roles = new HashMap<String, Role>();

	protected ScriptFactory scriptFactory;
	
	final Set<RpgTrait> npcs = new HashSet<RpgTrait>();
	
	private Set<ScriptFactoryPart> parts = new HashSet<ScriptFactoryPart>();
	
	public boolean verboseLogging = true;
	
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
		
		// Load config defaults
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		reloadConfig();
		
		// Load config metavalues
		verboseLogging = getConfig().getBoolean("verboseLogging");
		
		// Create SQL
		boolean sqlSuccess = connectSQL();
		if (!sqlSuccess) {
			return;
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
		printLogToConsole(log);
		
		// Add all custom traits
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(RpgTrait.class).withName("Rpgnpc"));
	}
	
	public boolean connectSQL() {
		boolean isSQLite = getConfig().getBoolean("useSQLite");
		
		String sqlitePathString = getConfig().getString("sqlitePath");
		
		if (!Paths.get(sqlitePathString).isAbsolute()) {
			sqlitePathString = (new File(getDataFolder(), sqlitePathString)).getAbsolutePath();
		}
		
		String sqlHost = getConfig().getString("SQL.host");
		String sqlPort = getConfig().getString("SQL.port");
		String sqlDatabase = getConfig().getString("SQL.database");
		String sqlUsername = getConfig().getString("SQL.username");
		String sqlPassword = getConfig().getString("SQL.password");
		
		try {
			if (isSQLite) {
				MySQL.connectSQLite(sqlitePathString);
			} else {
				MySQL.connect(sqlHost, sqlPort, sqlDatabase, sqlUsername, sqlPassword);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Bukkit.getLogger().log(Level.SEVERE, "Failed to establish connection with SQL database");
			
			Bukkit.getPluginManager().disablePlugin(this);
			
			return false;
		}
		
		return true;
	}
	
	public boolean disconnectSQL() {
		try {
			MySQL.disconnect();
		} catch (SQLException e) {
			e.printStackTrace();
			Bukkit.getLogger().log(Level.SEVERE, "Failed to disconnect from SQL database");
			
			Bukkit.getPluginManager().disablePlugin(this);
			
			return false;
		}
		
		return true;
	}
	
	public void printLogToConsole(ParseLog log) {
		String logString;
		if (verboseLogging) {
			logString = log.getFormattedString();
		} else {
			logString = log.getErrors().getFormattedString();
		}
		
		for (String string : logString.split("\n")) {
			getLogger().info(string);
		}
	}
	
	public static boolean hasPlaceholderAPI() {
		return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
	}
	
	public void addFactoryPart(ScriptFactoryPart part) {
		parts.add(part);
	}
	
	public void buildScriptFactory() {
		ScriptFactoryPart[] partsArray = parts.toArray(new ScriptFactoryPart[parts.size()]);
		scriptFactory = new ScriptFactory(partsArray, 0.8, 20, "§7");
	}

	public ParseLog reloadData() {
		// Unregister old trigger listeners
		for (Role role : roles.values()) {
			role.unregisterTriggerListeners(this);
		}
		
		// Unregister npcs
		for (RpgTrait npc : npcs) {
			unbindNpcTriggers(npc);
		}
		
		// Set new data
		reloadConfig();
		ConfigParser.ConfigResult result = ConfigParser.reloadConfig(scriptFactory, getConfig());
		roles = result.rolesMap;
		
		// Register trigger listeners
		for (Role role : roles.values()) {
			role.registerTriggerListeners(this);
		}
		
		// Register npcs
		for (RpgTrait npc : npcs) {
			bindNpcTriggers(npc);
		}
		
		return result.log;
	}
	
	public void registerRPGNPC(RpgTrait npc) {
		getLogger().info("Registering NPC " + npc.getNPC().getName());
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
