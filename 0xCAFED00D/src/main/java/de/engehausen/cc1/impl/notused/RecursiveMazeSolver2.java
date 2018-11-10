package de.engehausen.cc1.impl.notused;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Maze.Direction;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.challenge.MazeSolver;

/**
 * A recursive, back-tracking maze solver similar to the
 * actually used one. Instead of keeping the result path as
 * a list, this just remembers per visited location how to
 * get there. Afterwards, the path can be reconstructed. This
 * is what I think is done in Dijkstra's algorithm.
 * I tried this one, because I though it might be beneficial
 * in the parallel (two-threaded) version, see {@link DualThreadRecursiveMazeSolver2}.
 * However, it still held true that the single-threaded implementation
 * was faster.
 */
public class RecursiveMazeSolver2 implements MazeSolver {

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
		return buildPath(state, exit);
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
		for (Direction direction : nextDirections(current, state)) {
			if (state.maze.canGo(direction, current)) {
				final Position next = current.neighborAt(direction);
				if (!state.visited.containsKey(next)) {
					state.lastDirection = direction;
					if (recursiveSearch(state, next)) {
						return true;
					}
				}
			}
			// backtrack
			state.last = current;
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
		state.visited.put(position, state.last);
		state.last = position;
		return state.target.equals(position);
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
	 * Builds a path starting with the given end position. 
	 * @param state the current state, must not be <code>null</code>
	 * @param end the end position of the path
	 * @return a path ending in the given position, never <code>null</code>
	 */
	protected List<Position> buildPath(final State state, final Position end) {
		final LinkedList<Position> result = new LinkedList<>();
		Position pos = end;
		while (pos != null) {
			result.addFirst(pos);
			pos = state.visited.get(pos);
		}
		return result;
	}

	/**
	 * The search state, encapsulated into an object to
	 * avoid putting several objects on the stack during the recursion.
	 */
	public static class State {
		
		public final Maze maze;
		public final Map<Position, Position> visited;
		public final Position target;
		public Position last;
		public Direction lastDirection;
		
		protected State(final int sizeEstimate, final Maze maze, final Position exit) {
			this.maze = maze;
			target = exit;
			visited = new HashMap<>(4*sizeEstimate/3);
		}

	}

}
