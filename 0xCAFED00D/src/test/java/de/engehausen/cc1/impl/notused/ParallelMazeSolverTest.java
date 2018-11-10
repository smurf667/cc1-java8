package de.engehausen.cc1.impl.notused;

import org.junit.Before;

import de.engehausen.cc1.impl.MazeSolverTest;
import de.engehausen.cc1.impl.notused.ParallelMazeSolver;

public class ParallelMazeSolverTest extends MazeSolverTest {

	@Before
	public void setup() {
		solver = new ParallelMazeSolver();
	}

}
