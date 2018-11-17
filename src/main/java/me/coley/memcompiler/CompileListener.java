package me.coley.memcompiler;
/**
 * Wrapper for javax.tools.DiagnosticListener.
 */
public interface CompileListener {
	void report(CompilerMessage message);
}
