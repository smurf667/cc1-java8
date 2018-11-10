package de.engehausen.cc1.challenge.support;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.engehausen.cc1.challenge.MazeSolverTest;
import de.engehausen.cc1.challenge.SquaresTest;
import de.engehausen.cc1.challenge.TopTenWordsTest;
import de.engehausen.cc1.challenge.WordSpliteratorProviderTest;

/**
 * Executes all challenge tests for implementations located
 * in a "test" folder and writes a HTML report with the results.
 * <p>This is a bit hacky and messy. I ran a bit out of coding
 * virtue here, but it works - which is what counts!</p>
 */
public class Executor {

	/** file name of the JSON file capturing the test results */
	public static final String REPORT_JSON = "report.json";

	private static final String L_LN = "\n    ";
	private static final String LICENSE_INFO = 
		"\n" + L_LN + "Coding Challenge I: Java 8  Copyright (C) 2018  Jan Engehausen\n" +
		L_LN + "This program comes with ABSOLUTELY NO WARRANTY." +
		L_LN + "This is free software, and you are welcome to redistribute it" +
		L_LN + "under certain conditions; see source code for details.\n\n";

	/**
	 * A list with all tests.
	 */
	protected static final List<String> allTests = Arrays.asList(
		SquaresTest.class.getName(),
		TopTenWordsTest.class.getName(),
		WordSpliteratorProviderTest.class.getName(),
		MazeSolverTest.class.getName()
	);
	/** list of all test jars */
	private final List<File> jars;
	/** JVM launch arguments for the tests */
	private final String vmArgs;
	/** location of the driving jar, needed for separately launched Java processes */
	private final String selfJar;

	/**
	 * Runs all tests.
	 * @param args runner arguments; invoke without any to see what is required/possible.
	 * @throws Throwable in case of error
	 */
	public static void main(final String[] args) throws Throwable {
		System.out.println(LICENSE_INFO);
		if (args.length < 2) {
			System.out.println("usage: java -jar challenger-with-deps.jar <folder-of-jars-to-test> <report-folder> [\"<jvm-args-for-tests>\"]");
			return;
		}
		final File reportFolder = new File(getArgument(args, 1, "target/"));
		if (!reportFolder.mkdir() && !reportFolder.exists()) {
			throw new IllegalStateException(reportFolder.getCanonicalPath() + " cannot be created or is not a directory");
		}
		new Executor(
			new File(getArgument(args, 0, "C:\\Users\\engehau\\.m2\\repository\\de\\engehausen\\cc1\\0xCAFED00D\\0.1.0-SNAPSHOT")),
			getArgument(args, 2, "-server -Xms512m -Xmx512m -Xss32m")
		).runTests();
		final File testData = new File(REPORT_JSON);
//		testData.deleteOnExit();
		new Reporter().writeReport(testData, reportFolder);
	}

	private Executor(final File jarFolder, final String jvmArgs) throws IOException {
		vmArgs = jvmArgs;
		if (!jarFolder.exists()) {
			throw new IllegalArgumentException(jarFolder.getCanonicalPath() + " does not exist");
		}
		if (jarFolder.isDirectory()) {
			jars = Arrays.asList(
				jarFolder
					.listFiles(
						(dir, name) -> name.endsWith(".jar")
					)
				);
			if (jars.isEmpty()) {
				throw new IllegalArgumentException(jarFolder.getCanonicalPath() + " does not contain .jar files");
			}
		} else {
			throw new IllegalArgumentException(jarFolder.getCanonicalPath() + " ist not a directory");
		}
		selfJar = getSelfJar();
	}

	/**
	 * Runs tests against all files found in the test folder and
	 * writes a report file ({@link #REPORT_JSON}).
	 * @throws IOException in case of error
	 */
	protected void runTests() throws IOException {
		new File(REPORT_JSON).delete();
		// build the argument list (dynamic because of unknown number of jvm args)
		final List<String> args = new ArrayList<>();
		args.add("java");
		for (String arg : vmArgs.split(" ")) {
			args.add(arg);
		}
		args.add("-cp");
		final int cpIdx = args.size();
		args.add(null); // filled in for each jar
		args.add(TestRunner.class.getName());
		args.add(null); // filled in by runTest(String[])
		final String[] fixedArgs = args.toArray(new String[args.size()]);
		final String max = Integer.toString(jars.size());
		int step = 0;
		for (File jar : jars) {
			fixedArgs[cpIdx] = selfJar+File.pathSeparatorChar+jar.getCanonicalPath();
			step++;
			System.out.printf("Testing %s (%s/%s)%n", jar.getName(), Integer.toString(step), max);
			runTest(fixedArgs);
		}
	}

	/**
	 * Runs a single test in a new Java process.
	 * @param args the launch arguments
	 * @throws IOException in case of error.
	 */
	protected void runTest(final String[] args) throws IOException {
		final File log = new File("out.log");
		for (String testClass : allTests) {
			args[args.length-1] = testClass;
			final ProcessBuilder pb = new ProcessBuilder(args);
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.appendTo(log));
			System.out.printf("\t%s... ", testClass);
			final long then = System.nanoTime();
			Process p = pb.start();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				throw new IOException(e);
			} finally {
				System.out.printf("done (%ss)%n", Long.valueOf(Duration.ofNanos(System.nanoTime()-then).getSeconds()));
			}
		}
	}

	/**
	 * Determines the location of the <code>.jar</code> file
	 * containing this class. Required for launching new Java
	 * processes to perform the tests.
	 * @return the location of the archive containing this class
	 */
	private static String getSelfJar() {
		final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		final String classPath = runtime.getClassPath();
		if (classPath != null && classPath.length()>0) {
			return classPath;
		}
		return "target\\CodingChallenger-1.0-SNAPSHOT-jar-with-dependencies.jar";
	}
	
	private static String getArgument(final String[] args, final int idx, final String fallback) {
		if (idx >= args.length) {
			return fallback;
		}
		return args[idx];
	}

}
