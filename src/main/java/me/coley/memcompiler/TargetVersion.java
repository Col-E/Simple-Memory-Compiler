package me.coley.memcompiler;

/**
 * Wrapper for <i>-target</i> option.
 */
public enum TargetVersion {
	V4("1.4"), V5("1.5"), V6("1.6"), V7("1.7"), V8("1.8");

	/**
	 * Value to pass as a compiler argument.
	 */
	private final String opt;

	TargetVersion(String opt) {
		this.opt = opt;
	}

	@Override
	public String toString() {
		return opt;
	}
}
