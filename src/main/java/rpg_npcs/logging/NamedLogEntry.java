package rpg_npcs.logging;

import java.util.Map;

import org.bukkit.ChatColor;

public class NamedLogEntry implements LogEntry {
	private final String name;
	private final LogEntry entry;
	
	public NamedLogEntry(String name, LogEntry entry) {
		this.name = name;
		this.entry = entry;
	}

	@Override
	public int countErrors() {
		return entry.countErrors();
	}

	@Override
	public String getFormattedString(Map<MessageType, ChatColor> colours) {
		return name + ":\n" + entry.getFormattedString(colours).replace("\n", "\n  ");
	}

}
