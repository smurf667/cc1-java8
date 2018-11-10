package de.engehausen.cc1.challenge.support;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.engehausen.cc1.api.ArtifactDescriptor;
import de.engehausen.cc1.api.ArtifactDescriptor.Key;
import de.engehausen.cc1.challenge.SquaresTest;

/**
 * Executes one test class and adds the result to the {@link Executor#REPORT_JSON}
 * file.
 */
public class TestRunner extends RunListener implements Runnable {

	private static final String UNKNOWN_ARTIFACTID = "unknown";
	private static TestRunner instance;

	public static TestRunner getInstance() {
		return instance;
	}
	public static void createInstance(final String aTestClass) {
		try {
			instance = new TestRunner(aTestClass);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	private final ArtifactDescriptor descriptor;
	private final TestResult result;
	private final Class<?> testClass;
	private long lastNanos;
	private final boolean withDescription = false;
	private final ObjectMapper mapper;
	private final Map<String, List<TestResult>> reportData;

	private TestRunner(final String aTestClass) throws ClassNotFoundException {
		descriptor = createArtifactDescriptor();
		result = new TestResult();
		result.ok = new AtomicInteger();
		result.nok = new AtomicInteger();
		testClass = getTestClass(result, aTestClass, withDescription);
		mapper = new ObjectMapper();
		reportData = readReport(mapper);
	}
	
	private ArtifactDescriptor createArtifactDescriptor() {
		try {
			return createInstance(ArtifactDescriptor.class);
		} catch (IllegalStateException ignore) {
			// because I forgot to add the ArtifactDescriptor to the
			// services in the archetype, I have to do some nasty fallback here
			try {
				return (ArtifactDescriptor) Class.forName("de.engehausen.cc1.impl.ArtifactDescriptorImpl", true, Thread.currentThread().getContextClassLoader()).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	protected Map<String, List<TestResult>> readReport(final ObjectMapper objMapper) {
		final File report = new File(Executor.REPORT_JSON);
		if (report.exists()) {
			try {
				return objMapper.readValue(report, new TypeReference<Map<String, List<TestResult>>>() { /* empty */});
			} catch (IOException e) {
				return new HashMap<>();
			}
		}
		return new HashMap<>();
	}

	@Override
	public void run() {
		final JUnitCore core = new JUnitCore();
		core.addListener(this);
		core.run(testClass);
		Collections.sort(result.methods);
		try {
			String artifactId = descriptor.getPomKey(Key.artifactId);
			if (UNKNOWN_ARTIFACTID.equals(artifactId)) {
				// artifact does not describe itself, fall back to find a unique ID
				final Class<?> clazz = descriptor.getClass();
				final URL location = clazz.getResource('/'+clazz.getName().replace('.', '/')+".class");
				if (location != null) {
					String name = location.toExternalForm();
					int cut = name.indexOf('!');
					if (cut > 0) {
						name = name.substring(0, cut);
						cut = name.lastIndexOf('/');
						if (cut > 0) {							
							artifactId = name
								.substring(1+cut)
								.replace(".jar", "")
								.replace("-SNAPSHOT", "")
								.replace("-1.0", "");
						}
					}
				}
				if (UNKNOWN_ARTIFACTID.equals(artifactId)) {
					throw new IllegalStateException("unknown artifact ID, please fix .jar");
				}
			}

			final List<TestResult> list = reportData.get(artifactId);
			if (list == null) {
				reportData.put(artifactId, Collections.singletonList(result));
			} else {
				list.add(result);
			}
			mapper.writeValue(new File(Executor.REPORT_JSON), reportData);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public void reportPerformance(final long nanos) {
		lastNanos = nanos;
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		getCurrent().trace = failure.getTrace();
		result.nok.incrementAndGet();
		result.ok.decrementAndGet();
		lastNanos = 0;
	}

	@Override
	public void testFinished(final Description description) throws Exception {
		final TestDescription info = description.getAnnotation(TestDescription.class);
		if (info.performanceTest() && lastNanos > 0) {
			final TestResult current = getCurrent();
			current.millis = Double.valueOf((lastNanos/1000000d));
		}
		result.ok.incrementAndGet();
	}

	@Override
	public void testStarted(final Description description) throws Exception {
		lastNanos = 0;
		final TestResult current = new TestResult();
		final TestDescription info = description.getAnnotation(TestDescription.class);
		current.name = description.getMethodName();
		System.out.printf("running %s...%n", description.getDisplayName());
		if (withDescription) {
			current.description = info.description();
		}
		result.methods.add(current);
	}

	private static Class<?> getTestClass(final TestResult result, final String clzName, final boolean describe) throws ClassNotFoundException {
		final Class<?> testClass = Class.forName(clzName, true, Thread.currentThread().getContextClassLoader());
		result.name = testClass.getSimpleName();
		final TestDescription info = testClass.getAnnotation(TestDescription.class);
		if (describe) {
			result.description = info.description();
		}
		result.methods = new ArrayList<>();
		return testClass;
	}

	private TestResult getCurrent() {
		return result.methods.get(result.methods.size()-1);
	}

	public static void main(final String[] args) throws Throwable {
		createInstance((args != null && args.length > 0) ? args[0] : SquaresTest.class.getName());
		instance.run();
	}

	public <T> T createInstance(final Class<T> clazz) {
		final ServiceLoader<T> loader = ServiceLoader.load(clazz);
		final Iterator<T> services = loader.iterator();
		if (services.hasNext()) {
			final T service = services.next();
			if (services.hasNext()) {
				throw new IllegalStateException("class loader contains more than one implementation for "+clazz.getName());
			}
			return service;
		} else {
			throw new IllegalStateException("no implementation found for "+clazz.getName());
		}
	}

}
