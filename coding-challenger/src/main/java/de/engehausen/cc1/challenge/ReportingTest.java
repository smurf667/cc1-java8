package de.engehausen.cc1.challenge;

import java.time.Duration;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;

import de.engehausen.cc1.challenge.support.TestRunner;

/**
 * Base test class with some additional features.
 * @param <T> the type of test this is for.
 */
public class ReportingTest<T> {

	private static long TIMEOUT = 3*60*1000L; // time out after three minutes

	protected final T instance;
	private long bestTime;

	/**
	 * Creates the test for the given test implementation.
	 * @param clazz the implementation to test.
	 */
	public ReportingTest(final Class<T> clazz) {
		if (TestRunner.getInstance() == null) {
			// running as JUnit test in IDE
			TestRunner.createInstance(getClass().getName());
		}
		instance = TestRunner.getInstance().createInstance(clazz);
	}

	@Before
	public void init() {
		bestTime = Long.MAX_VALUE;
	}

	@After
	public void report() {
		if (bestTime != Long.MAX_VALUE) {
			System.out.printf("\t%sms%n", Double.valueOf(bestTime/1000000d));
			TestRunner.getInstance().reportPerformance(bestTime);
		}
	}

	/**
	 * Returns the best execution time recorded via {@link #measure(Runnable)}.
	 * @return the best execution time.
	 */
	protected Duration getBestExecutionTime() {
		return Duration.ofNanos(bestTime);
	}

	/**
	 * Measures the execution time for the given runnable
	 * and stores the execution time if smaller than the last one
	 * recorded.
	 * @param test the test to perform, must not be <code>null</code>.
	 */
	protected <V> V measure(final Callable<V> test) {
		final long then = System.nanoTime();
		try {
			return test.call();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			final long delta = System.nanoTime() - then;
			if (delta < bestTime) {
				bestTime = delta;
			}
		}
	}

	/**
	 * Repeater helper for performance measurements.
	 * In conjunction with {@link #measure(Callable)} a best-of-seven
	 * measurement is performed.
	 * @param code the code to execute.
	 */
	protected void repeat(final Runnable code) {
		for (int i = 0; i < 7; i++) {
			code.run();
		}
	}

	/**
	 * Executes the given code with a time limit, see {@link #TIMEOUT}.
	 * @param code the code to execute
	 * @throws Throwable in case of error
	 */
	protected void runWithTimeout(final Runnable code) throws Throwable {
		runWithTimeout(TIMEOUT, code);
	}

	/**
	 * Executes the given code with a time limit, see {@link #TIMEOUT}.
	 * @param timeout timeout in milliseconds
	 * @param code the code to execute
	 * @throws Throwable in case of error
	 */
	@SuppressWarnings("deprecation")
	protected void runWithTimeout(final long timeout, final Runnable code) throws Throwable {
		final boolean[] done = { false };
		final Throwable[] cause = new Throwable[1];
		final Thread worker = new Thread(() -> {
			try {
				code.run();
			} catch (Throwable t) {
				cause[0] = t;
			} finally {
				done[0] = true;
				synchronized (cause) {
					cause.notifyAll();
				}
			}
		});
		worker.start();
		long remaining = timeout;
		final long end = System.currentTimeMillis() + remaining;
		while (!done[0] && remaining > 0) {
			try {
				synchronized (cause) {
					cause.wait(remaining);
				}
				remaining = end - System.currentTimeMillis();
			} catch (InterruptedException e) {
				remaining = end - System.currentTimeMillis();
			}
		}
		if (!done[0]) {
			worker.stop(); // despite being deprecated, the thread must be forced to stop now
			throw new RuntimeException("Performance test time-out after "+(TIMEOUT/1000L)+" seconds");
		} else if (cause[0] != null) {
			throw cause[0];
		}
		
	}

}
