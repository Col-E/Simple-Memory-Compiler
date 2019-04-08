# Simple Memory Compiler [![](https://jitpack.io/v/Col-E/Simple-Memory-Compiler.svg)](https://jitpack.io/#Col-E/Simple-Memory-Compiler)

This is a basic wrapper for the `javax.tools.JavaCompiler`.

* For Java 8- this is found in `tools.jar`.
* For Java 9+ this is found in the module `java.compiler`.

### Features

* Support for multiple source files / classes
* Managable compiler flags
    * Wrappers for: _classpath, target-version, debug-information, verbosity_

### Examples

**Basic example:**
```java
// source code to compile
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
Compiler c = new JavaXCompiler();
c.addUnit("HelloWorld", s.toString());
c.compile();
// compiled code, note the additional inner class.
byte[] outer = c.getUnitCode("HelloWorld");
byte[] inner = c.getUnitCode("HelloWorld$A");
System.out.println(Arrays.toString(outer));
System.out.println(Arrays.toString(inner));
```

**Specifying custom classpath:**
```java
Compiler c = ...
// by default the current runtime's 'java.class.path' is used
// you can specify additional paths like so:
c.setClassPath(Collections.singletonList("lib/MyLibrary.jar"));
// lib is a folder in the current directory
```

**Targeting older versions of java:**
```java
Compiler c = ...
// by default it'll just use the current runtime version
c.setTarget(TargetVersion.V6);
```

**Configuring debug information:**
```java
Compiler c = ...
// by default debug information is not included
// setting the booleans in debug will auto-gen the correct flags for attribute inclusion
c.getDebug().lineNumbers = true;
```

### Using in your project

This project is hosted via JitPack.io. You can add this project to your maven project like so:
```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
<dependencies>
	<dependency>
	    <groupId>com.github.Col-E</groupId>
	    <artifactId>Simple-Memory-Compiler</artifactId>
	    <version>2.1</version>
	</dependency>
</dependencies>
```

### Building

Pre-built: 

* [releases](https://github.com/Col-E/Simple-Memory-Compiler/releases)

Build-yourself: 

* clone / download the repo
* open a terminal in the directory with `pom.xml`
* run `mvn package`
    * Generates jar file in `/target` directory