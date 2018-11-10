package de.engehausen.cc1.impl;

import org.junit.Before;

/**
 * Tests the single-threaded, recursive maze solver.
 */
public class RecursiveMazeSolverTest extends MazeSolverTest {
	
	@Before
	public void setup() {
		solver = new RecursiveMazeSolver();
	}

}
