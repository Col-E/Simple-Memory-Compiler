package me.coley.memcompiler.test;

import me.coley.memcompiler.Compiler;
import me.coley.memcompiler.TargetVersion;

import java.util.Arrays;

public class Test {
	public static void main(String[] args) {
		testInner();
		testDebug();
	}

	/**
	 * Test generation of inner class units.
	 */
	private static void testInner() {
		// source code
		StringBuilder s = new StringBuilder();
		s.append("public class HelloWorld {" +
				"  public static void main(String args[]) {" +
				"    A.print(\"Hello from an inner class\");" +
				"  }" +
				"  public static class A {" +
				"    public static void print(String s){" +
				"       System.out.println(s);" +
				"    }" +
				"  }" +
				"}");
		// create the compiler, add the code
		Compiler c = new Compiler();
		c.addUnit("HelloWorld", s.toString());
		c.compile();
		// compiled code
		byte[] outer = c.getUnitCode("HelloWorld");
		byte[] inner = c.getUnitCode("HelloWorld$A");
		// output
		if(outer == null || inner == null) {
			throw new RuntimeException("Failed to compile");
		}
		System.out.println(Arrays.toString(outer));
		System.out.println(Arrays.toString(inner));
	}

	/**
	 * Test generation of inner class units.
	 */
	private static void testDebug() {
		// source code
		StringBuilder s = new StringBuilder();
		s.append("public class HelloWorld {" +
				"  public static void main(String args[]) {" +
				"    String a = \"Hello from \";" +
				"    String b = \"an inner class\";" +
				"    System.out.print(a + b);" +
				"  }" +
				"}");
		// create the compilers, add the code
		Compiler cDebug = new Compiler();
		cDebug.addUnit("HelloWorld", s.toString());
		cDebug.getDebug().lineNumbers = true;
		cDebug.getDebug().variables = true;
		cDebug.getDebug().sourceName = true;
		cDebug.setTarget(TargetVersion.V6);
		System.out.println(cDebug.compile());
		Compiler cNone = new Compiler();
		cNone.addUnit("HelloWorld", s.toString());
		System.out.println(cNone.compile());
		// compiled code
		byte[] debug = cDebug.getUnitCode("HelloWorld");
		byte[] nodebug = cNone.getUnitCode("HelloWorld");
		// output
		if(debug == null || nodebug == null || debug.length == 0 || nodebug.length == 0) {
			throw new RuntimeException("Failed to compile");
		}
		if(Arrays.equals(debug, nodebug)) {
			throw new RuntimeException("Debug information not differentiated!");
		}
		System.out.println(Arrays.toString(debug));
		System.out.println(Arrays.toString(nodebug));
	}
}
