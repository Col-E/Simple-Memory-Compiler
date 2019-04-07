package me.coley.memcompiler;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * In memory java-to-bytecode compiler.
 */
public class JavaXCompiler implements Compiler {
	/**
	 * Additional items to append to the classpath when compiling.
	 */
	private List<String> pathItems;
	/**
	 * Root modules to be used for module resolution.
	 */
	private List<String> modules;
	/**
	 * Compilation unit map.
	 */
	private final Map<String, VirtualJavaFileObject> unitMap = new HashMap<>();
	/**
	 * Listens to compile errors.
	 */
	private final ProxyDirectedListener listener = new ProxyDirectedListener();
	/**
	 * Javac option wrapper: Inclusion of debugging attributes.
	 */
	private final DebugInfo debug = new DebugInfo();
	/**
	 * Version of java to target.
	 */
	private TargetVersion target = TargetVersion.V8;

	@Override
	public boolean compile() {
		JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		// file manager, used so that the unit map can have their definitions updated
		// after compilation.
		JavaFileManager fmFallback = javac.getStandardFileManager(listener, Locale
				.getDefault(), StandardCharsets.UTF_8);
		JavaFileManager fm = new VirtualFileManager(fmFallback);
		// add options
		List<String> options = new ArrayList<>();
		if(pathItems != null && pathItems.size() > 0) {
			options.addAll(Arrays.asList("-classpath", getClassPathText()));
		}
		options.addAll(Arrays.asList("-source", target.toString()));
		options.addAll(Arrays.asList("-target", target.toString()));
		options.add(debug.toOption());
		// create task
		try {
			JavaCompiler.CompilationTask task = javac.getTask(null, fm, listener,
					options, null, unitMap.values());
			if(modules != null && modules.size() > 0) {
				if(jvmSupportsModules()) {
					Class compilationTaskClass = JavaCompiler.CompilationTask.class;
					Method addModules = compilationTaskClass.getMethod("addModules", List.class);
					addModules.invoke(task, modules);
				} else {
					// should probably report some kind of warning
				}
			}
			return task.call();
		} catch(RuntimeException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}


	/**
	 * @return true if this Java version supports modules
	 */
	public static boolean jvmSupportsModules() {
		double version = getVersion();
		return version > 1.8;
	}

	private static double getVersion() {
		String version = System.getProperty("java.version");
		int pos = version.indexOf('.');
		if (pos == -1)
			return Double.parseDouble(version);
		pos = version.indexOf('.', pos + 1);
		return Double.parseDouble(version.substring(0, pos));
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

	@Override
	public void addUnit(String className, String content) {
		unitMap.put(className, new VirtualJavaFileObject(className, content));
	}

	@Override
	public Set<String> getUnitNames() {
		return unitMap.keySet();
	}

	@Override
	public byte[] getUnitCode(String name) {
		VirtualJavaFileObject file = unitMap.get(name);
		if(file == null) {
			return null;
		}
		return file.getBytecode();
	}

	@Override
	public String getUnitSource(String name) {
		VirtualJavaFileObject file = unitMap.get(name);
		if(file == null) {
			return null;
		}
		return file.getSource();
	}

	@Override
	public List<String> getClassPath() {
		return pathItems;
	}

	@Override
	public void setClassPath(List<String> pathItems) {
		this.pathItems = pathItems;
	}

	@Override
	public List<String> getModules() {
		return modules;
	}

	@Override
	public void setModules(List<String> modules) {
		this.modules = modules;
	}

	@Override
	public TargetVersion getTarget() {
		return target;
	}

	@Override
	public void setTarget(TargetVersion target) {
		this.target = target;
	}

	@Override
	public DebugInfo getDebug() {
		return debug;
	}

	@Override
	public CompileListener getCompileListener() {
		return listener.getProxy();
	}

	@Override
	public void setCompileListener(CompileListener proxy) {
		this.listener.setProxy(proxy);
	}

	/**
	 * Basic listener that allows directed diagnostic logging.
	 */
	private final class ProxyDirectedListener implements DiagnosticListener {
		private CompileListener proxy;

		public CompileListener getProxy() {
			return proxy;
		}

		public void setProxy(CompileListener proxy) {
			this.proxy = proxy;
		}

		@Override
		public void report(Diagnostic diagnostic) {
			proxy.report(new CompilerMessage(diagnostic));
		}
	}

	/**
	 * File manager extension for handling updates to java file object's output stream.
	 * Additionally, registers inner classes as new files.
	 */
	private final class VirtualFileManager extends ForwardingJavaFileManager {
		private VirtualFileManager(JavaFileManager fallback) {
			super(fallback);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String name, Kind
				kind, FileObject sibling) throws IOException {
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
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE
					.extension), Kind.SOURCE);
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
