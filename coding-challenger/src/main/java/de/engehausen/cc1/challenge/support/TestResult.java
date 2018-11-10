package de.engehausen.cc1.challenge.support;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

public class TestResult implements Comparable<TestResult> {
	public String name;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	public AtomicInteger ok;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	public AtomicInteger nok;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	public String description;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	public String trace;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	public Double millis;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	public List<TestResult> methods;

	@Override
	public int compareTo(final TestResult o) {
		final double a = getValue(millis);
		final double b = getValue(o.millis);
		if (a == b) {
			return name.compareTo(o.name);
		}
		return (int) (b-a);
	}

	private double getValue(final Double d) {
		if (d == null) {
			return Double.MAX_VALUE;
		} else {
			return d.doubleValue();
		}
	}
	
}