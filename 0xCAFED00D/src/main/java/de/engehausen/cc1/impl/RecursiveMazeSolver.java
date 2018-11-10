package de.engehausen.cc1.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Maze.Direction;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.challenge.MazeSolver;

/**
 * <p>A recursive, rather naive back-tracking maze solver.
 * I did this <em>after</em> all the fancy implementations found
 * in the <code>notused</code> sub-package. It turns out this
 * implementation is often much faster and way more readable.
 * I once more fell victim to Knuth's famous "premature optimization
 * is the root of all evil".</p>
 * <p><object type="image/svg+xml" data="doc-files/maze64_single.svg" width="512" height="512"><param name="src" value="doc-files/maze64_single.svg"></object></p>
 * <p>There is a variant of this solver that tries to search from
 * both start-to-end and vice versa directions in parallel: {@link de.engehausen.cc1.impl.notused.DualThreadRecursiveMazeSolver}.
 * Unfortunately, in tests it seems like the single-threaded one is almost always
 * faster still. Here is a diagram of tests for square mazes solved with both
 * implementations. The dual-threaded implementation is rarely faster:</p>
 * <img src="doc-files/s_vs_d.png" alt="single vs. dual">
 * <p>The problem becomes even more obvious when the relative performance of
 * the dual-threaded implementation is compared against the single-threaded one:</p>
 * <img src="doc-files/s_vs_d_rel.png" alt="relative performance">
 * @see de.engehausen.cc1.impl.notused
 */
public class RecursiveMazeSolver implements MazeSolver {

	private static final Map<Direction, Direction[]> DIRECTIONS;
	
	static {
		DIRECTIONS = new HashMap<>();
		final Direction[] all = Direction.values();
		DIRECTIONS.put(null, all);
		final List<Direction> asList = Arrays.asList(all);
		for (Direction last : all) {
			DIRECTIONS.put(last, removeOpposite(last, asList));
		}
	}
	
	private static Direction[] removeOpposite(final Direction last, final List<Direction> all) {
		final Set<Direction> asSet = new HashSet<>(all);
		switch (last) {
			case UP:
				asSet.remove(Direction.DOWN);
				break;
			case DOWN:
				asSet.remove(Direction.UP);
				break;
			case LEFT:
				asSet.remove(Direction.RIGHT);
				break;
			case RIGHT:
				asSet.remove(Direction.LEFT);
				break;
			default:
				break;
		}
		return asSet.toArray(new Direction[asSet.size()-1]);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Position> getEscapeRoute(final Maze maze, final Position start, final Position exit) throws IllegalStateException {
		final State state = new State(estimateResultPathSize(maze, start, exit), maze, exit);
		if (!recursiveSearch(state, start)) {
			throw new IllegalStateException("there is no route to the exit");
		}
		return state.path;
	}

	/**
	 * Recursive search through the maze held by the state, starting from the given position.
	 * @param state the current state; this is used to held a number of objects so that these
	 * do not have to be put on the stack (i.e. as method arguments) individually.
	 * @param current the current position
	 * @return <code>true</code> if the target position given in the state has been reached,
	 * <code>false</code> otherwise
	 */
	protected boolean recursiveSearch(final State state, final Position current) {
		if (visit(state, current)) {
			return true;
		}
		final int last = state.path.size();
		for (Direction direction : nextDirections(current, state)) {
			if (state.maze.canGo(direction, current)) {
				final Position next = current.neighborAt(direction);
				if (!state.visited.contains(next)) {
					state.lastDirection = direction;
					if (recursiveSearch(state, next)) {
						return true;
					}
					backtrack(state, last);
				}
			}
		}
		return false;
	}

	/**
	 * Visits the given position using the current state.
	 * Adds the given position to the current search path.
	 * @param state the current state
	 * @param position the position to visit
	 * @return <code>true</code> if the target position has been reached,
	 * <code>false</code> otherwise
	 */
	protected boolean visit(final State state, final Position position) {
		state.path.add(position);
		state.visited.add(position);
		return state.target.equals(position);
	}

	/**
	 * Removes the last position from the current search path.
	 * @param state the current state
	 * @param idx the index of the position to remove
	 * @return the removed position
	 */
	protected Position backtrack(final State state, int idx) {
		return state.path.remove(idx);
	}

	/**
	 * Returns an array with the directions to continue searching in.
	 * @param current the current position
	 * @param state the current state
	 * @return all directions except the one that would go back to where
	 * the search just came from. Sub-classes may decide on a "better"
	 * strategy given the current position and state
	 */
	protected Direction[] nextDirections(final Position current, final State state) {
		// return all directions except the opposite from which we came
		return DIRECTIONS.get(state.lastDirection);
	}

	/**
	 * Estimates the size of the escape route. This is simply the sum
	 * of the delta in both horizontal and vertical directions between
	 * the given start and end points plus twenty percent extra.
	 * @param maze the given maze, must not be <code>null</code>
	 * @param start the starting position, must not be <code>null</code>
	 * @param exit the target position, must not be <code>null</code>
	 * @return an estimate of the size of the escape route
	 */
	protected int estimateResultPathSize(final Maze maze, final Position start, final Position exit) {
		final int result = Math.abs(start.getX()-exit.getX()) + Math.abs(start.getY()-exit.getY());
		if (result > 0) {
			return 12*result/10;
		}
		// gotcha: Math.abs(int) can return a negative value!
		return 100;
	}

	/**
	 * The search state, encapsulated into an object to
	 * avoid putting several objects on the stack during the recursion.
	 */
	public static class State {
		
		public final Maze maze;
		public final List<Position> path;
		public final Set<Position> visited;
		public final Position target;
		public Direction lastDirection;
		
		protected State(final int sizeEstimate, final Maze maze, final Position exit) {
			this.maze = maze;
			target = exit;
			path = new ArrayList<>(sizeEstimate);
			visited = new HashSet<>(4*sizeEstimate/3);
		}

	}

}
