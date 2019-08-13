package rpg_npcs.logging;

import java.util.Map;

import org.bukkit.ChatColor;

public interface LogEntry {
	public enum MessageType {
		INFO,
		ERROR
	}
	
	public int countErrors();
	
	public String getFormattedString(Map<LogEntry.MessageType, ChatColor> colours);
}
