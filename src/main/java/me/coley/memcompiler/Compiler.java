package me.coley.memcompiler;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * In memory java-to-bytecode compiler.
 */
public class Compiler {
	/**
	 * Additional items to append to the classpath when compiling.
	 */
	private final List<String> pathItems = new ArrayList<>();
	/**
	 * Compilation unit map.
	 */
	private final Map<String, VirtualJavaFileObject> unitMap = new HashMap<>();
	/**
	 * Output stream for diagnostic logging. Defaults to system-out.
	 */
	private PrintStream out = System.out;
	/**
	 * Javac option wrapper: Inclusion of debugging attributes.
	 */
	private final DebugInfo debug = new DebugInfo();
	/**
	 * Print out additional information.
	 */
	private boolean verbose;
	/**
	 * Version of java to target.
	 */
	private TargetVersion target = TargetVersion.V8;

	/**
	 * @return Success of compilation.
	 */
	public boolean compile() {
		JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		DiagnosticListener<JavaFileObject> listener = new LoggingListener();
		// file manager, used so that the unit map can have their definitions updated after compilation.
		JavaFileManager fmFallback = javac.getStandardFileManager(listener, Locale.getDefault(), StandardCharsets.UTF_8);
		JavaFileManager fm = new VirtualFileManager(fmFallback);
		// add options
		List<String> options = new ArrayList<>();
		options.addAll(Arrays.asList("-classpath", getClassPathText()));
		options.addAll(Arrays.asList("-target", target.toString()));
		options.add(debug.toOption());
		if(verbose)
			options.add("-verbose");
		// create task
		try {
			JavaCompiler.CompilationTask task = javac.getTask(null, fm, listener, options, null, unitMap.values());
			return task.call();
		} catch(RuntimeException e) {
			return false;
		}
	}

	/**
	 * @return Generated classpath.
	 */
	private String getClassPathText() {
		// ensure the default path is included
		String pathDefault = System.getProperty("java.class.path");
		StringBuilder sb = new StringBuilder(pathDefault);
		// add extra dependencies
		for(String path : pathItems) {
			sb.append(";" + path);
		}
		return sb.toString();
	}

	/**
	 * Add class to compilation process.
	 *
	 * @param className
	 * 		Name of class to compile.
	 * @param content
	 * 		Source code of class.
	 */
	public void addUnit(String className, String content) {
		unitMap.put(className, new VirtualJavaFileObject(className, content));
	}

	/**
	 * @return Set of class names being compiled.
	 */
	public Set<String> getUnitNames() {
		return unitMap.keySet();
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Bytecode of class.
	 */
	public byte[] getUnitCode(String name) {
		VirtualJavaFileObject file = unitMap.get(name);
		if(file == null) {
			return null;
		}
		return file.getBytecode();
	}

	/**
	 * @param name
	 * 		Class name.
	 *
	 * @return Source code of class.
	 */
	public String getUnitSource(String name) {
		VirtualJavaFileObject file = unitMap.get(name);
		if(file == null) {
			return null;
		}
		return file.getSource();
	}


	/**
	 * List for additional classpath dependencies.
	 * The current runtime's 'java.class.path' property is appended to this
	 * list during the compilation process.
	 *
	 * @return Class path items.
	 */
	public List<String> getClassPath() {
		return pathItems;
	}

	/**
	 * @return Is compiler printing verbose messages.
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param verbose
	 * 		Wheter compiler should verbosely print messages.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Version of java that the compiler will generate bytecode for.
	 *
	 * @return Targeted version.
	 */
	public TargetVersion getTarget() {
		return target;
	}

	/**
	 * See {@link #getTarget()} for details.
	 *
	 * @param target
	 * 		Targeted version.
	 */
	public void setTarget(TargetVersion target) {
		this.target = target;
	}

	/**
	 * @return Wrapper for what debug information to emit in compiled bytecode.
	 */
	public DebugInfo getDebug() {
		return debug;
	}

	/**
	 * @return Diagnostic output stream.
	 */
	public PrintStream getOut() {
		return out;
	}

	/**
	 * Set diagnostic output stream.
	 *
	 * @param out
	 * 		New diagnostic output stream.
	 */
	public void setOut(PrintStream out) {
		this.out = out;
	}

	/**
	 * Basic listener that allows directed diagnostic logging.
	 */
	private final class LoggingListener implements DiagnosticListener {
		@Override
		public void report(Diagnostic diagnostic) {
			if(out != null) {
				out.println(diagnostic.toString());
			}
		}
	}

	/**
	 * File manager extension for handling updates to java file object's output stream. Additionally, registers inner classes as new files.
	 */
	private final class VirtualFileManager extends ForwardingJavaFileManager {
		private VirtualFileManager(JavaFileManager fallback) {
			super(fallback);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject sibling) throws IOException {
			VirtualJavaFileObject obj = unitMap.get(name);
			// Unknown class, assumed to be an inner class.
			// add to the unit map so it can be fetched.
			if(obj == null) {
				obj = new VirtualJavaFileObject(name, null);
				unitMap.put(name, obj);
			}
			return obj;
		}
	}

	/**
	 * Java file extension that keeps track of the compiled bytecode.
	 */
	private static final class VirtualJavaFileObject extends SimpleJavaFileObject {
		/**
		 * Output to contain compiled bytcode.
		 */
		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		/**
		 * Content of source file to compile.
		 */
		private final String content;

		public VirtualJavaFileObject(String className, String content) {
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.content = content;
		}

		/**
		 * @return Compiled bytecode of class.
		 */
		private byte[] getBytecode() {
			return baos.toByteArray();
		}

		/**
		 * @return Class source code.
		 */
		private String getSource() {
			return content;
		}

		@Override
		public final OutputStream openOutputStream() throws IOException {
			return baos;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return content;
		}
	}
}
