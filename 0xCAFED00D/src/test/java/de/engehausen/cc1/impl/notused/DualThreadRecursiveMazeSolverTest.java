package de.engehausen.cc1.impl.notused;

import org.junit.Before;

import de.engehausen.cc1.impl.MazeSolverTest;
import de.engehausen.cc1.impl.notused.DualThreadRecursiveMazeSolver;

public class DualThreadRecursiveMazeSolverTest extends MazeSolverTest {
	
	@Before
	public void setup() {
		solver = new DualThreadRecursiveMazeSolver();
	}

}
