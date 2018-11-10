package de.engehausen.cc1.challenge.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TestDescription {

	/**
	 * TestDescription of the test or method.
	 * @return the description of the test or method.
	 */
	String description();

	/**
	 * Whether this test is a performance test or not.
	 * @return <code>true</code> if a performance test, <code>false</code> otherwise.
	 */
	boolean performanceTest() default false;

}
