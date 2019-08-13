package rpg_npcs.logging;

import java.util.Map;

import org.bukkit.ChatColor;

public class StringLogEntry implements LogEntry {
	private final String messageString;
	private final MessageType messageType;
	
	public StringLogEntry(String messageString, MessageType messageType) {
		this.messageString = messageString;
		this.messageType = messageType;
	}

	@Override
	public int countErrors() {
		if (messageType.equals(MessageType.ERROR)) {
			return 1;
		}
		
		return 0;
	}

	@Override
	public String getFormattedString(Map<MessageType, ChatColor> colours) {
		String colourCode = colours.containsKey(messageType) ? colours.get(messageType).toString() : "";
		return colourCode + messageString;
	}
}