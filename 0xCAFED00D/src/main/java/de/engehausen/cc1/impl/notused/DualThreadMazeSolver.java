package de.engehausen.cc1.impl.notused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.challenge.MazeSolver;

/**
 * A back-tracking maze solver using two threads. The solver tries to
 * find routes from the start and from the exit position at the same time.
 * If the two paths meet during the search, the search is aborted and the
 * paths combined, yielding the escape route.
 * <p><img id="animSingle" src="../doc-files/maze64_dual.svg" alt="view image in browser to see animation" width="512" height="512"></p>
 */
public class DualThreadMazeSolver implements MazeSolver {

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
		final PathList firstList = new PathList();
		final PathList secondList = new PathList();
		final Thread first = new Thread(
			() -> getEscapeRoute(firstList, secondList, maze, start, exit)
		);
		first.start();
		final Thread second = new Thread(
			() -> getEscapeRoute(secondList, firstList, maze, exit, start)
		);
		second.start();
		try {
			first.join();
			second.join();
		} catch (InterruptedException e) {
			// interrupted, can't give result - fall back to single-threaded resolver
			return new MazeSolverImpl().getEscapeRoute(maze, start, exit);
		}
		if (firstList.contains(exit)) {
			// first list contains path from start to exit, just return it
			return firstList.getPositions();
		}
		if (secondList.contains(start)) {
			// second list contains path from exit to start, reverse it, then return it
			final List<Position> route = secondList.getPositions();
			Collections.reverse(route);
			return route;
		}
		// neither first list nor second list contain a full path, but there is a combination
		final Set<Position> intersect = firstList.getPositionsAsSet();
		intersect.retainAll(secondList.getPositionsAsSet());
		if (intersect.isEmpty()) {
			throw new IllegalStateException("cannot find route");
		}
		
		// merge the two paths into one result path
		final Position commonPosition = intersect.iterator().next(); // find the position where both paths meet
		
		// overestimate initial size (no resizing required later...)
		final List<Position> result = new ArrayList<>(firstList.size() + secondList.size());
		
		Iterator<Position> iterator = firstList.getPositions().iterator();
		while (iterator.hasNext()) {
			final Position current = iterator.next();
			result.add(current);
			if (commonPosition.equals(current)) {
				break;
			}
		}
		iterator = new LinkedList<>(secondList.getPositions()).descendingIterator();
		while (iterator.hasNext() && !commonPosition.equals(iterator.next()));
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
		if (intersect.size() > 1) {
			System.err.printf("??? WTF result %s%n", result);
		}
		return result;
	}
	
	protected void getEscapeRoute(final PathList path, final PathList partner, final Maze maze, final Position start, final Position exit) {
		final Map<Position, List<Position>> candidates = new HashMap<>();// new LinkedHashMap<>();
		path.add(start);
		Position position = start;
		int offset = 0; //position.hashCode();
		while (!exit.equals(position) && partner.isActive()) {
			if (partner.contains(position)) {
				// the partner thread has a path which includes our
				// current position, we can merge, quickly exit!
				break;
			}
			List<Position> neighbors = candidates.get(position);
			if (neighbors == null) {
				neighbors = new ArrayList<>(NEIGHBOR_COUNT);
				// "randomly" chose a sequence of directions to go to
//				for (Maze.Direction direction : DIRECTIONS[Math.abs(position.hashCode())%DIRECTIONS.length]) {
				for (Maze.Direction direction : DIRECTIONS[Math.abs(offset++)%DIRECTIONS.length]) {
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
				path.removeLast(); // dead end
				if (path.isEmpty()) {
					throw new IllegalStateException("no route found");
				}
				candidates.put(position, Collections.emptyList());
				position = path.getLast();
			} else {
				path.add(position = neighbors.remove(idx));
			}
		}
		path.stop();
		partner.stop();
	}

	/**
	 * Optimized, semi-synchronized list of positions.
	 * Holds all elements of the list in a set for fast <code>contains()</code>
	 * operations. Methods using during the building of the list
	 * are synchronized, others not. In general, this is not thread-safe, but
	 * as used in this class it is safe.
	 */
	private static class PathList {

		private final Set<Position> positions;
		private final List<Position> list;
		private volatile boolean active;
		
		protected PathList() {
			positions = new HashSet<>();
			list = new ArrayList<>(100);
			active = true;
		}

		/**
		 * Returns the size of the list. Must not be used
		 * while the list is still being built.
		 * @return the size of the list.
		 */
		protected int size() {
			return list.size();
		}

		/**
		 * Returns the list of positions. Must only be called
		 * after the list has been fully built.
		 * @return the list of positions.
		 */
		protected List<Position> getPositions() {
			return list;
		}

		/**
		 * Returns all positions of the list as a set.
		 * Must only be called after the list has been fully built.
		 * @return all positions of the list as a set.
		 */
		protected Set<Position> getPositionsAsSet() {
			return positions;
		}

		/**
		 * Checks whether or not the current list is empty.
		 * May be called from any thread.
		 * @return <code>true</code> if the list is empty,
		 * <code>false</code> otherwise.
		 */
		protected synchronized boolean isEmpty() {
			return list.isEmpty();
		}

		/**
		 * Returns the last position of the list.
		 * May be called from any thread.
		 * @return the last position of the list.
		 * @throws IndexOutOfBoundsException if there is no last position
		 */
		protected synchronized Position getLast() {
			return list.get(list.size()-1);
		}

		/**
		 * Removes and returns the last position of the list.
		 * May be called from any thread.
		 * @return the last position of the list, may be <code>null</code>
		 * @throws IndexOutOfBoundsException if there is no last position
		 */
		protected Position removeLast() {
			if (active) {
				synchronized (this) {
					final Position result = list.remove(list.size()-1);
					positions.remove(result);
					return result;
				}
			}
			return null;
		}

		/**
		 * Adds the given position as the last element to the list.
		 * May be called from any thread.
		 * @param position the position to add
		 * @return <code>true</code> if the position was added, <code>false</code> otherwise.
		 */
		protected boolean add(final Position position) {
			if (active) {
				synchronized (this) {
					positions.add(position);
					return list.add(position);
				}
			}
			return false;
		}

		/**
		 * Tests whether the given position is part of the list.
		 * May be called from any thread.
		 * @param position the position to check for
		 * @return <code>true</code> if the position is contained in the list, <code>false</code> otherwise.
		 */
		protected boolean contains(final Position position) {
			final boolean result;
			synchronized (this) {
				result = positions.contains(position);
			}
			if (result == true) {
				stop();
			}
			return result;
		}

		/**
		 * Indicates whether this list is active, i.e. if it can
		 * be modified. The list becomes inactive once {@link #stop()}
		 * has been called.
		 * May be called from any thread.
		 * @return <code>true</code> if the list is active, <code>false</code> otherwise
		 */
		protected boolean isActive() {
			return active;
		}

		/**
		 * Makes the list inactive.
		 * May be called from any thread.
		 */
		protected void stop() {
			active = false;
		}
	}
	
}
