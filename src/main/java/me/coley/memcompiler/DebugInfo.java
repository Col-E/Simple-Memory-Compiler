package me.coley.memcompiler;

/**
 * Wrapper for debug attribute inclusion <i>(-g option</i>
 *
 * @author Matt
 */
public class DebugInfo {
	/**
	 * Debug attributes.
	 */
	public boolean variables, lineNumbers, sourceName;

	/**
	 * @return Javac argument format.
	 */
	public String toOption() {
		// generate options
		String options = getOptions();
		if(options.length() > 0) {
			return "-g:" + options;
		}
		// default to none
		return "-g:none";
	}

	private String getOptions() {
		StringBuilder s = new StringBuilder();
		if(variables)
			s.append("vars,");
		if(lineNumbers)
			s.append("lines,");
		if(sourceName)
			s.append("source");
		// substr off dangling comma
		String value = s.toString();
		if(value.endsWith(",")) {
			value = s.substring(0, value.length() - 1);
		}
		return value;
	}
}
