package de.engehausen.cc1.impl.notused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.challenge.MazeSolver;

/**
 * A back-tracking, single-threaded maze solver. This is not recursive
 * and might be better for large mazes, as it would not rely on the stack
 * to hold all depths of recursion. Additional operations however seem
 * to make it slower than the recursive implementations.
 * <p><img id="animSingle" src="doc-files/maze64_single.svg" alt="view image in browser to see animation" width="512" height="512"></p>
 */
public class MazeSolverImpl implements MazeSolver {

	private static final int NEIGHBOR_COUNT = Maze.Direction.values().length - 1; // max number of neighbors

	// all permutations for directions, used to "randomize" the selection
	// of directions to test for each step in looking for the exit
	private static final Maze.Direction[][] DIRECTIONS;
	
	static {
		final List<Maze.Direction[]> permutations = new PermutationGenerator<Maze.Direction>(Maze.Direction.values()).all();
		DIRECTIONS = permutations.toArray(new Maze.Direction[permutations.size()][]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Position> getEscapeRoute(final Maze maze, final Position start, final Position exit) {
		final Map<Position, List<Position>> candidates = new HashMap<>();
		final List<Position> path = new ArrayList<>(100); // used LinkedList before, but this seems faster still
		path.add(start);
		Position position = start;
		int offset = 0; // "randomizer" for the list of directions to try next
		while (!exit.equals(position)) {
			List<Position> neighbors = candidates.get(position);
			if (neighbors == null) {
				neighbors = new ArrayList<>(NEIGHBOR_COUNT);
				// "randomly" choose a sequence of directions to go to
				for (Maze.Direction direction : DIRECTIONS[Math.abs(offset)%DIRECTIONS.length]) {
					if (maze.canGo(direction, position)) {
						final Position neighbor = position.neighborAt(direction);
						if (!candidates.containsKey(neighbor)) {
							neighbors.add(neighbor);
						}
					}
				}
				candidates.put(position, neighbors);
			}
			final int idx = neighbors.size() - 1;
			if (idx < 0) {
				final int last = path.size()-1;
				path.remove(last); // dead end
				if (path.isEmpty()) {
					throw new IllegalStateException("no route found");
				}
				candidates.put(position, Collections.emptyList()); // free memory by replacing empty list with a singleton instance
				position = path.get(last-1);
			} else {
				path.add(position = neighbors.remove(idx));
			}
		}
		return path;
	}
}
