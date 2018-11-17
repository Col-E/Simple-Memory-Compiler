package me.coley.memcompiler.test;

import me.coley.memcompiler.CompileListener;
import me.coley.memcompiler.CompilerMessage;
import me.coley.memcompiler.JavaXCompiler;
import me.coley.memcompiler.TargetVersion;
import org.junit.Test;

import static org.junit.Assert.*;

public class CompilerTests {
	private final static CompileListener FAIL_ON_ERROR = message -> {
		assertTrue("Threw an unexpected error!", message.getType() != CompilerMessage
				.Type.ERROR);
	};

	/**
	 * Tests generation of inner class units.
	 */
	@Test
	public void testInner() {
		// source code
		StringBuilder s = new StringBuilder();
		s.append("public class HelloWorld {" +
				"  public static void main(String args[])" +
				"  {" +
				"    A.print(\"Hello from an inner class\");" +
				"  }" +
				"  public" +
				" static class A {" +
				"    public static void print(String s){" +
				"        System.out.println(s);" +
				"    }" +
				"  }" +
				"}");
		// create the compiler, add the code
		JavaXCompiler c = new JavaXCompiler();
		c.addUnit("HelloWorld", s.toString());
		c.setCompileListener(FAIL_ON_ERROR);
		assertTrue("Failed to compile!", c.compile());
		// compiled code
		byte[] outer = c.getUnitCode("HelloWorld");
		byte[] inner = c.getUnitCode("HelloWorld$A");
		assertNotNull("Missing unit for outer class", outer);
		assertNotNull("Missing unit for inner class", inner);
	}

	/**
	 * Tests generation of inner class units.
	 */
	@Test
	public void testDebug() {
		// source code
		StringBuilder s = new StringBuilder();
		s.append("public class HelloWorld {" +
				"  public static void main(String args[])" +
				"  {" +
				"    String a = \"Hello \";" +
				"    String b = \"World!\";" +
				"    System.out.print(a + b);" +
				"  }" +
				"}");
		// create the compilers, add the code
		JavaXCompiler cDebug = new JavaXCompiler();
		cDebug.addUnit("HelloWorld", s.toString());
		cDebug.getDebug().lineNumbers = true;
		cDebug.getDebug().variables = true;
		cDebug.getDebug().sourceName = true;
		cDebug.setTarget(TargetVersion.V8);
		cDebug.setCompileListener(FAIL_ON_ERROR);
		cDebug.compile();
		JavaXCompiler cNone = new JavaXCompiler();
		cNone.addUnit("HelloWorld", s.toString());
		cNone.setCompileListener(FAIL_ON_ERROR);
		cNone.compile();
		// compiled code
		byte[] debug = cDebug.getUnitCode("HelloWorld");
		byte[] nodebug = cNone.getUnitCode("HelloWorld");
		assertNotNull("Missing unit for debug class", debug);
		assertNotNull("Missing unit for non-debug class", nodebug);
		assertTrue("Debug class is NOT larger than stripped class!", debug.length >
				nodebug.length);
	}
}
