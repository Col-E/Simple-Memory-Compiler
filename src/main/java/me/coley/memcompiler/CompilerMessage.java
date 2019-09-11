package me.coley.memcompiler;

import javax.tools.Diagnostic;
import java.util.Locale;

/**
 * Wrapper for JavaX Diagnostic.
 *
 * @author Matt
 */
public class CompilerMessage {
	private final Diagnostic diagnostic;

	CompilerMessage(Diagnostic diagnostic) {
		this.diagnostic = diagnostic;
	}

	/**
	 * @return The type of message.
	 */
	public Type getType() {
		switch(diagnostic.getKind()) {
			case ERROR:
				return Type.ERROR;
			case WARNING:
				return Type.WARNING;
			case MANDATORY_WARNING:
				return Type.MANDATORY_WARNING;
			case NOTE:
				return Type.NOTE;
			case OTHER:
				return Type.OTHER;
		}
		return Type.OTHER;
	}

	/**
	 * @return Column index the error occurs on.
	 */
	public long column() {
		return diagnostic.getColumnNumber();
	}

	/**
	 * @return Line index the error occurs on.
	 */
	public long line() {
		return diagnostic.getLineNumber();
	}

	/**
	 * @return Index in the source code the error starts at.
	 */
	public long start() {
		return diagnostic.getStartPosition();
	}

	/**
	 * @return Index in the source code the error ends at.
	 */
	public long end() {
		return diagnostic.getEndPosition();
	}

	/**
	 * @return The error message, if one exists. May be null.
	 */
	public String message() {
		return message(Locale.getDefault());
	}

	/**
	 * @param locale
	 * 		Locale to localise message into.
	 *
	 * @return The error message, if one exists. May be null.
	 */
	public String message(Locale locale) {
		return diagnostic.getMessage(locale);
	}

	@Override
	public String toString() {
		return getType().name() + ":[" + line() + ":" + column() + "] \"" + message() + "\"";
	}

	/**
	 * The type of message.
	 */
	public enum Type {
		/**
		 * Fatal error, prevents compilation.
		 */
		ERROR, /**
		 * Improper practice, but compilation still passes.
		 */
		WARNING, /**
		 * Improper practice, but compilation still passes.
		 */
		MANDATORY_WARNING, /**
		 * Informative message, compilation still passes.
		 */
		NOTE, /**
		 * Unknown message type.
		 */
		OTHER,
	}
}
