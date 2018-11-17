package me.coley.memcompiler;

import java.util.List;
import java.util.Set;

public interface Compiler {
	/**
	 * @return Success of compilation. Use {@link #setCompileListener(CompileListener)}
	 * to receive information about failures.
	 */
	boolean compile();

	/**
	 * Add class to compilation process.
	 *
	 * @param className
	 * 		Name of class to compile.
	 * @param content
	 * 		Source code of class.
	 */
	void addUnit(String className, String content);

	/**
	 * @return Set of class names being compiled.<br><b>Note on inner classes:</b> If a
	 * class that has an inner class is supplied in {@link #addUnit(String, String)}
	 * running {@link #compile()} will  update the unit-map. The inner class will not
	 * have source code available via  {@link #getUnitSource(String)}, but its bytecode
	 * will be available via {@link #getUnitCode(String)}.
	 */
	Set<String> getUnitNames();

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Bytecode of class.
	 */
	byte[] getUnitCode(String name);

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Source code of class.
	 */
	String getUnitSource(String name);

	/**
	 * List for additional classpath dependencies.
	 * The current runtime's 'java.class.path' property is appended to this
	 * list during the compilation process.
	 *
	 * @return Class path items.
	 */
	List<String> getClassPath();

	/**
	 * @param pathItems
	 * 		Items to use for the classpath in compilation.
	 */
	void setClassPath(List<String> pathItems);

	/**
	 * @return Root modules to be used for module resolution.
	 */
	List<String> getModules();

	/**
	 * @param modules
	 * 		Root modules to be used for module resolution.
	 */
	void setModules(List<String> modules);

	/**
	 * Version of java that the compiler will generate bytecode for.
	 *
	 * @return Targeted version.
	 */
	TargetVersion getTarget();

	/**
	 * See {@link #getTarget()} for details.
	 *
	 * @param target
	 * 		Targeted version.
	 */
	void setTarget(TargetVersion target);

	/**
	 * @return Wrapper for what debug information to emit in compiled bytecode.
	 */
	DebugInfo getDebug();

	/**
	 * @return Listener that receives compiler error information.
	 */
	CompileListener getCompileListener();

	/**
	 * Set a listener to receive compiler error information.
	 *
	 * @param proxy
	 * 		Listener that receives compiler error information.
	 */
	void setCompileListener(CompileListener proxy);
}
