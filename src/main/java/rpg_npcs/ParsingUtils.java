package rpg_npcs;

import java.util.regex.Pattern;

public class ParsingUtils {
	public static final String POSITIVE_REGEX_STRING = "(?:1|t(?:rue)?|on|y(?:es)?)";
	public static final String NEGATIVE_REGEX_STRING = "(?:0|f(?:alse)?|off|n(?:o)?)";
	
	public static boolean isPositive(String text) {
		return Pattern.matches(POSITIVE_REGEX_STRING, text);
	}
	
	public static boolean isNegative(String text) {
		return Pattern.matches(NEGATIVE_REGEX_STRING, text);
	}
}
