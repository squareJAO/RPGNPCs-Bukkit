package rpg_npcs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;

public class ParseLog {
	public static class LogEntry {
		public enum Type {
			INFO,
			ERROR
		}
		
		public final String messageString;
		public final Type messageType;
		
		public LogEntry(String messageString, Type messageType) {
			super();
			this.messageString = messageString;
			this.messageType = messageType;
		}
	}
	
	private final List<ParseLog.LogEntry> entries = new LinkedList<ParseLog.LogEntry>();
	
	public void add(ParseLog log) {
		entries.addAll(log.entries);
	}
	
	public int errorCount() {
		int errorCount = 0;
		
		for (LogEntry logEntry : entries) {
			if (logEntry.messageType == LogEntry.Type.ERROR) {
				errorCount++;
			}
		}
		
		return errorCount;
	}
	
	public void addInfo(String messageString) {
		addEntry(messageString, LogEntry.Type.INFO);
	}
	
	public void addError(String messageString) {
		addEntry(messageString, LogEntry.Type.ERROR);
	}
	
	public void addEntry(String messageString, LogEntry.Type messageType) {
		addEntry(new LogEntry(messageString, messageType));
	}
	
	public void addEntry(LogEntry message) {
		entries.add(message);
	}
	
	public String getFormattedString(Map<LogEntry.Type, ChatColor> colours) {
		String outputString = "";
		
		for (LogEntry logEntry : entries) {
			String colourCode = colours.containsKey(logEntry.messageType) ? colours.get(logEntry.messageType).toString() : "";
			outputString += colourCode + logEntry.messageString + "\n";
		}
		
		return outputString;
	}
	
	public String getFormattedString() {
		Map<LogEntry.Type, ChatColor> colours = new HashMap<ParseLog.LogEntry.Type, ChatColor>();
		colours.put(LogEntry.Type.INFO, ChatColor.WHITE);
		colours.put(LogEntry.Type.ERROR, ChatColor.RED);
		
		return getFormattedString(colours);
	}

	public ParseLog getErrors() {
		ParseLog errorLog = new ParseLog();

		for (LogEntry logEntry : entries) {
			if (logEntry.messageType == LogEntry.Type.ERROR) {
				errorLog.addError(logEntry.messageString);
			}
		}
		
		return errorLog;
	}
}
