package de.engehausen.cc1.impl.notused;

import java.util.List;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.challenge.MazeSolver;
import de.engehausen.cc1.impl.RecursiveMazeSolver;

/**
 * A maze solver delegating between the single-threaded {@link RecursiveMazeSolver}
 * (for estimated smaller mazes) and the dual-threaded {@link DualThreadRecursiveMazeSolver}
 * (for estimated larger mazes).
 */
public class MazeSolverDelegator implements MazeSolver {

	// the other implementations could be plugged in here too
	private final MazeSolver singleThreaded = new RecursiveMazeSolver();
	private final MazeSolver dualThreaded = new DualThreadRecursiveMazeSolver();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Position> getEscapeRoute(final Maze maze, final Position start, final Position exit) throws IllegalStateException {
		// use heuristics to delegate
		if (Math.max(
			Math.abs(start.getX()-exit.getX()),
			Math.abs(start.getY()-exit.getY())
		) < 512) {
			// this is probably a "small" maze, use the (probably faster) single threaded solver
			return singleThreaded.getEscapeRoute(maze, start, exit);
		} else {
			// looks big, use the dual-threaded solver...
			return dualThreaded.getEscapeRoute(maze, start, exit);
		}
	}

}
