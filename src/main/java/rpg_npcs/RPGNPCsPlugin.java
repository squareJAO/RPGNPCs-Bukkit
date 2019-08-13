package rpg_npcs;

import java.beans.PropertyVetoException;
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

import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import rpg_npcs.command.CommadCalculate;
import rpg_npcs.command.CommandEditRpgNpc;
import rpg_npcs.logging.Log;
import rpg_npcs.prerequisite.RangePrerequisite;
import rpg_npcs.prerequisite.StatePrerequisite;
import rpg_npcs.prerequisite.magic.HoldingWandPrerequisite;
import rpg_npcs.role.Role;
import rpg_npcs.script.factoryPart.ScriptFactoryCommandPart;
import rpg_npcs.script.factoryPart.ScriptFactoryPausePart;
import rpg_npcs.script.factoryPart.ScriptFactoryQuestionPart;
import rpg_npcs.script.factoryPart.ScriptFactorySpeedPart;
import rpg_npcs.script.factoryPart.ScriptFactoryStatusPart;
import rpg_npcs.script.node.command.ScriptCommandNode;
import rpg_npcs.script.node.command.ScriptCrouchNode;
import rpg_npcs.script.node.command.ScriptExecuteCommandNode;
import rpg_npcs.script.node.command.ScriptForceLookCommandNode;
import rpg_npcs.script.node.command.ScriptLookCloseNode;
import rpg_npcs.script.node.command.ScriptOpenShopCommandNode;
import rpg_npcs.script.node.command.ScriptStoreNode;
import rpg_npcs.sql.MyPostgreSQL;
import rpg_npcs.sql.MySQL;
import rpg_npcs.sql.MySQLite;
import rpg_npcs.state.NumberStateType;
import rpg_npcs.state.StateNpcScope;
import rpg_npcs.state.StatePlayerScope;
import rpg_npcs.state.StateWorldScope;
import rpg_npcs.trigger.BreakBlockTrigger;
import rpg_npcs.trigger.MoveTrigger;
import rpg_npcs.trigger.RightClickTrigger;

public class RPGNPCsPlugin extends JavaPlugin {
	public static MySQL sql;
	
	public Map<String, Role> roles = new HashMap<String, Role>();
	public final Set<RpgNpc> npcs = new HashSet<RpgNpc>();

	protected ParserFactorySet factorySet;
	
	// Config settings
	public boolean verboseLogging = true;
	public int defaultConversationMaxRange = 5;
	public int ticksPerRangeCheck = 15;
	
	// Accessible script parts
	private ScriptFactoryCommandPart scriptFactoryCommandPart;
	
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
		int defaultTriggerPriority = getConfig().getInt("defaultTriggerPriority");
		factorySet = new ParserFactorySet(defaultTriggerPriority);
		scriptFactoryCommandPart = new ScriptFactoryCommandPart(factorySet);
		addDefaultFactoryData();
		
		// Load all conversations
		Log log = reload();
		printLogToConsole(log);
		
		// Add all custom traits
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(RpgTrait.class).withName("Rpgnpc"));
		
		// Reload config
		reload();
	}
	
	private void addDefaultFactoryData() {
		// Command factory part
		addCommandNodeGenerator("crouch", ScriptCrouchNode.class);
		addCommandNodeGenerator("look(?:close)?", ScriptLookCloseNode.class);
		addCommandNodeGenerator("(?:force)?goggle", ScriptForceLookCommandNode.class);
		addCommandNodeGenerator("(?:store|set)(?:in)?", ScriptStoreNode.class);
		addCommandNodeGenerator("run|execute", ScriptExecuteCommandNode.class);
		addCommandNodeGenerator("(?:open)?shop", ScriptOpenShopCommandNode.class);
		
		// Add all factory parts
		factorySet.addScriptFactoryPart(new ScriptFactoryPausePart());
		factorySet.addScriptFactoryPart(new ScriptFactorySpeedPart());
		factorySet.addScriptFactoryPart(scriptFactoryCommandPart);
		factorySet.addScriptFactoryPart(new ScriptFactoryQuestionPart());
		factorySet.addScriptFactoryPart(new ScriptFactoryStatusPart(factorySet));
		
		// Supported types
		factorySet.addSupportedStateType(new NumberStateType());
		
		// Supported scopes
		factorySet.addSupportedStateScope(new StateNpcScope());
		factorySet.addSupportedStateScope(new StatePlayerScope());
		factorySet.addSupportedStateScope(new StateWorldScope());
		
		// Prerequisites
		factorySet.addSupportedPrerequisite("range|distance", RangePrerequisite.class);
		factorySet.addSupportedPrerequisite("state|var(?:iable)|value", StatePrerequisite.class);
		factorySet.addSupportedPrerequisite("holdingwand", HoldingWandPrerequisite.class);
		
		// Triggers
		factorySet.addSupportedTrigger("(?:player)?move", MoveTrigger.class);
		factorySet.addSupportedTrigger("(?:player)?rightclick", RightClickTrigger.class);
		factorySet.addSupportedTrigger("(?:player)?break(?:block)?", BreakBlockTrigger.class);
	}
	
	public void addCommandNodeGenerator(String pattern, Class<? extends ScriptCommandNode> commandClass) {
		scriptFactoryCommandPart.addCommandNodeGenerator(pattern, commandClass);
	}
	
	private void createSQL() {
		boolean isSQLite = getConfig().getBoolean("useSQLite");
		
		if (isSQLite) {
			String sqlitePathString = getConfig().getString("sqlitePath");
			if (!Paths.get(sqlitePathString).isAbsolute()) {
				sqlitePathString = (new File(getDataFolder(), sqlitePathString)).getAbsolutePath();
			}
			
			sql = new MySQLite(sqlitePathString);
		} else {
			String sqlUrl = getConfig().getString("SQL.url");
			String sqlUsername = getConfig().getString("SQL.username");
			String sqlPassword = getConfig().getString("SQL.password");
			try {
				sql = new MyPostgreSQL(sqlUrl, sqlUsername, sqlPassword);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
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
	
	public void printLogToConsole(Log log) {
		String logString;
		if (verboseLogging) {
			logString = log.getFormattedString();
		} else {
			logString = log.getErrors().getFormattedString();
		}
		
		if (logString != "") {
			for (String string : logString.split("\n")) {
				getLogger().info(string);
			}
		}
	}

	public Log reload() {
		// Unregister old trigger listeners
		for (Role role : roles.values()) {
			role.unregisterTriggerListeners(this);
		}
		
		// Reload base config
		reloadConfig();
		FileConfiguration config = getConfig();
		
		// Load folders
		addFolderSection(config, "triggers");
		addFolderSection(config, "states");
		addFolderSection(config, "roles");
		
		// Load metavalues
		verboseLogging = config.getBoolean("verboseLogging");
		defaultConversationMaxRange = config.getInt("defaultConversationMaxRange");
		ticksPerRangeCheck = config.getInt("ticksPerRangeCheck");
		factorySet.setCharactersPerWrap(config.getInt("charactersPerLine"));
		factorySet.setDefaultLineStartString(ChatColor.translateAlternateColorCodes('&', config.getString("lineStartString")));
		factorySet.setDefaultSpeed(config.getDouble("defaultTextSpeed"));
		factorySet.setDefaultTriggerPriority(getConfig().getInt("defaultTriggerPriority"));
		
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
	
	private void addFolderSection(Configuration config, String groupNameString) {
		ConfigurationSection groupConfigSection = config.getConfigurationSection(groupNameString);
		if (groupConfigSection == null) {
			groupConfigSection = config.createSection(groupNameString);
		}
		File folder = new File(getDataFolder(), groupNameString);
		if (!folder.exists()) {
			folder.mkdir();
		}
		File[] files = folder.listFiles();
		for (File file : files) {
			Configuration fileConfiguration = YamlConfiguration.loadConfiguration(file);
			String itemNameString = FilenameUtils.getBaseName(file.getName());
			
			ConfigurationSection itemConfigSection;
			if (groupConfigSection.isConfigurationSection(itemNameString)) {
				itemConfigSection = groupConfigSection.getConfigurationSection(itemNameString);
			} else {
				itemConfigSection = groupConfigSection.createSection(itemNameString);
			}

			// Move role data into place
			for (String key : fileConfiguration.getKeys(true)) {
				if (!fileConfiguration.isConfigurationSection(key)) {
					itemConfigSection.set(key, fileConfiguration.get(key));
				}
			}
		}
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
	
	public static Plugin getPlaceholderAPI() {
		return Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
	}
	
	public static Plugin getShop() {
		return Bukkit.getPluginManager().getPlugin("Shop");
	}
	
	public static Plugin getMagic() {
		return Bukkit.getPluginManager().getPlugin("Magic");
	}
	
	public static RPGNPCsPlugin getPlugin() {
		return (RPGNPCsPlugin) Bukkit.getPluginManager().getPlugin("RPGNPCs");
	}
	
	public ParserFactorySet getParserFactorySet() {
		return factorySet;
	}
}
