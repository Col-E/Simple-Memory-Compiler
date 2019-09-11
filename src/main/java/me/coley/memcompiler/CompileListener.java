package me.coley.memcompiler;
/**
 * Wrapper for javax.tools.DiagnosticListener.
 *
 * @author Matt
 */
public interface CompileListener {
	void report(CompilerMessage message);
}
