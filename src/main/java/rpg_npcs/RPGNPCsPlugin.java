package rpg_npcs;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import rpg_npcs.command.CommadCalculate;
import rpg_npcs.command.CommandEditRpgNpc;
import rpg_npcs.role.Role;
import rpg_npcs.script.factoryPart.ScriptFactoryCommandPart;
import rpg_npcs.script.factoryPart.ScriptFactoryPausePart;
import rpg_npcs.script.factoryPart.ScriptFactoryQuestionPart;
import rpg_npcs.script.factoryPart.ScriptFactorySpeedPart;
import rpg_npcs.script.factoryPart.ScriptFactoryStatusPart;
import rpg_npcs.script.node.command.ScriptCrouchNode;
import rpg_npcs.script.node.command.ScriptLookCloseNode;
import rpg_npcs.script.node.command.ScriptStoreNode;
import rpg_npcs.state.NumberStateType;

public class RPGNPCsPlugin extends JavaPlugin {
	public static MySQL sql;
	
	public Map<String, Role> roles = new HashMap<String, Role>();
	public final Set<RpgNpc> npcs = new HashSet<RpgNpc>();

	protected ParserFactorySet factorySet;
	
	// Config settings
	public boolean verboseLogging = true;
	public int defaultConversationMaxRange = 5;
	public int ticksPerRangeCheck = 15;
	
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
		
		// Create SQL
		createSQL();
		
		// Add commands
		CommandEditRpgNpc commandEditRpgNpc = new CommandEditRpgNpc(this);
		getCommand("RPGNPC").setTabCompleter(commandEditRpgNpc);
		getCommand("RPGNPC").setExecutor(commandEditRpgNpc);
		CommadCalculate commandCalculate = new CommadCalculate();
		getCommand("calculate").setTabCompleter(commandCalculate);
		getCommand("calculate").setExecutor(commandCalculate);
		
		// Create default factory set
		factorySet = new ParserFactorySet();
		addDefaultFactoryData();
		
		// Load all conversations
		ParseLog log = reload();
		printLogToConsole(log);
		
		// Add all custom traits
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(RpgTrait.class).withName("Rpgnpc"));
		
		// Reload config
		reload();
	}
	
	private void addDefaultFactoryData() {
		// Command factory part
		ScriptFactoryCommandPart scriptFactoryCommandPart = new ScriptFactoryCommandPart();
		scriptFactoryCommandPart.addCommandNodeGenerator("crouch", ScriptCrouchNode.class);
		scriptFactoryCommandPart.addCommandNodeGenerator("look(?:close)?", ScriptLookCloseNode.class);
		scriptFactoryCommandPart.addCommandNodeGenerator("store(?:in)?", ScriptStoreNode.class);
		
		// Add all factory parts
		factorySet.addScriptFactoryPart("pause", new ScriptFactoryPausePart());
		factorySet.addScriptFactoryPart("speed", new ScriptFactorySpeedPart());
		factorySet.addScriptFactoryPart("command", scriptFactoryCommandPart);
		factorySet.addScriptFactoryPart("question", new ScriptFactoryQuestionPart());
		factorySet.addScriptFactoryPart("status", new ScriptFactoryStatusPart());
		
		// Supported types
		factorySet.addSupportedStateType(new NumberStateType());
	}

	private void createSQL() {
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
		
		if (isSQLite) {
			sql = MySQL.makeSQLite(sqlitePathString);
		} else {
			sql = MySQL.makeSQL(sqlHost, sqlPort, sqlDatabase, sqlUsername, sqlPassword);
		}
		
		// Create tables
		try {
			Connection connection = sql.connect();
			PreparedStatement statement = connection.prepareStatement(
					"CREATE TABLE IF NOT EXISTS global_states (" + 
					"state_uuid TEXT PRIMARY KEY," + 
					"value TEXT )");
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
		}
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

	public ParseLog reload() {
		// Unregister old trigger listeners
		for (Role role : roles.values()) {
			role.unregisterTriggerListeners(this);
		}
		
		// Reload
		reloadConfig();
		
		// Load metavalues
		verboseLogging = getConfig().getBoolean("verboseLogging");
		defaultConversationMaxRange = getConfig().getInt("defaultConversationMaxRange");
		ticksPerRangeCheck = getConfig().getInt("ticksPerRangeCheck");
		factorySet.setCharactersPerWrap(getConfig().getInt("charactersPerLine"));
		factorySet.setDefaultLineStartString(getConfig().getString("lineStartString"));
		factorySet.setDefaultSpeed(getConfig().getDouble("defaultTextSpeed"));
		
		// Rebuild
		factorySet.rebuild();
		
		// Set new data
		ConfigParser.ConfigResult result = factorySet.getConfigParser().reloadConfig(getConfig());
		roles = result.rolesMap;
		
		// Register trigger listeners
		for (Role role : roles.values()) {
			role.registerTriggerListeners(this);
		}
		
		// Register npcs
		for (RpgNpc npc : npcs) {
			setNPCRole(npc);
		}
		
		return result.log;
	}
	
	public void registerRPGNPC(RpgNpc npc) {
		getLogger().info("Registering NPC " + npc.getNPCName());
		npcs.add(npc);
		setNPCRole(npc);
	}
	
	public void unregisterRPGNPC(RpgNpc npc) {
		npcs.remove(npc);
		npc.setRole(null);
	}
	
	private void setNPCRole(RpgNpc npc) {
		String npcName = npc.getNPCName();
		if (roles.containsKey(npcName)) {
			npc.setRole(roles.get(npcName));
		} else {
			npc.setRole(roles.get(Role.DEFAULT_ROLE_NAME_STRING));
		}
	}
	
	public RpgNpc getSelectedRpgNpc(CommandSender sender) {
		// Check for citizens2
		NPC selectedNpc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
		
		if (selectedNpc != null) {
			if (selectedNpc.hasTrait(RpgTrait.class)) {
				return selectedNpc.getTrait(RpgTrait.class);
			}
		}
		
		return null;
	}
}
