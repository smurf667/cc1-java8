package de.engehausen.cc1.impl.notused;

import org.junit.Before;

import de.engehausen.cc1.impl.MazeSolverTest;
import de.engehausen.cc1.impl.notused.DualThreadMazeSolver;

public class DualThreadMazeSolverTest extends MazeSolverTest {

	@Before
	public void setup() {
		solver = new DualThreadMazeSolver();
	}

}
