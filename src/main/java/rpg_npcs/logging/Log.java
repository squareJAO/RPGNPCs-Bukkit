package rpg_npcs.logging;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;

public class Log implements LogEntry {
	
	private final List<LogEntry> entries = new LinkedList<LogEntry>();

	@Override
	public int countErrors() {
		int errorCount = 0;
		
		for (LogEntry logEntry : entries) {
			errorCount += logEntry.countErrors();
		}
		
		return errorCount;
	}
	
	public void addInfo(String messageString) {
		addEntry(messageString, MessageType.INFO);
	}
	
	public void addError(String messageString) {
		addEntry(messageString, MessageType.ERROR);
	}
	
	public void addNamedEntry(String name, LogEntry entry) {
		addEntry(new NamedLogEntry(name, entry));
	}
	
	public void addEntry(String messageString, MessageType messageType) {
		addEntry(new StringLogEntry(messageString, messageType));
	}
	
	public void addEntry(LogEntry message) {
		entries.add(message);
	}
	
	public String getFormattedString(Map<MessageType, ChatColor> colours) {
		return entries.stream().map(entry -> entry.getFormattedString(colours)).collect(Collectors.joining("\n"));
	}
	
	public String getFormattedString() {
		Map<MessageType, ChatColor> colours = new HashMap<MessageType, ChatColor>();
		colours.put(MessageType.INFO, ChatColor.WHITE);
		colours.put(MessageType.ERROR, ChatColor.RED);
		
		return getFormattedString(colours);
	}

	public Log getErrors() {
		Log errorLog = new Log();

		for (LogEntry logEntry : entries) {
			if (logEntry.countErrors() > 0) {
				errorLog.addEntry(logEntry);
			}
		}
		
		return errorLog;
	}

	public static Log fromString(String localizedMessage, MessageType messageType) {
		Log log = new Log();
		log.addEntry(localizedMessage, messageType);
		return log;
	}
}
