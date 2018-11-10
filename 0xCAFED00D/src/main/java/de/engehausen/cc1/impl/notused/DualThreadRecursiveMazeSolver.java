package de.engehausen.cc1.impl.notused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.impl.RecursiveMazeSolver;

/**
 * A recursive, back-tracking maze solver using two threads. One thread
 * searches from start to exit, the other from exit to start. If the two
 * paths meet during the search, the search is aborted and the
 * paths combined, yielding the escape route.
 * <p><object type="image/svg+xml" data="../doc-files/maze64_dual.svg" width="512" height="512"><param name="src" value="../doc-files/maze64_dual.svg"></object></p>
 */
public class DualThreadRecursiveMazeSolver extends RecursiveMazeSolver {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Position> getEscapeRoute(final Maze maze, final Position start, final Position exit) throws IllegalStateException {
		final int estimatedSize = estimateResultPathSize(maze, start, exit);
		final DualState forward = new DualState(estimatedSize, maze, exit);
		final DualState backward = new DualState(estimatedSize, maze, start);
		forward.other = backward;
		backward.other = forward;
		final Thread one = new Thread(() -> super.recursiveSearch(forward, start));
		one.start();
		final Thread two = new Thread(() -> super.recursiveSearch(backward, exit));
		two.start();
		try {
			one.join();
			two.join();
		} catch (InterruptedException e) {
			// give up, search single-threaded
			return super.getEscapeRoute(maze, start, exit);
		}
		if (forward.pathAsSet.contains(exit)) {
			return forward.path;
		} else if (backward.pathAsSet.contains(start)) {
			Collections.reverse(backward.path);
			return backward.path;
		}
		// try to merge paths
		final Set<Position> intersect = forward.pathAsSet;
		intersect.retainAll(backward.pathAsSet);
		if (intersect.isEmpty()) {
			throw new IllegalStateException("cannot find route");
		}
		// both paths share at least one common position, merge
		// them into the result path
		final Position common = intersect.iterator().next();
		final List<Position> result = new ArrayList<>(forward.path.size() + backward.path.size());
		Iterator<Position> iterator = forward.path.iterator();
		Position current;
		do {
			current = iterator.next();
			result.add(current);
		} while (!common.equals(current));
		// walk back from the common position to the exit on the backwards list
		iterator = new LinkedList<>(backward.path).descendingIterator();
		while (!common.equals(iterator.next()));
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
		return result;
	}

	/**
	 * Visit the given position. Calls {@link RecursiveMazeSolver#visit(State, Position)}
	 * and adds the given position to the set of positions currently making
	 * up the path.
	 * @param state the current search state
	 * @param position the position to visit.
	 * @return <code>true</code> if the target position has been reached,
	 * or the other search thread and this search thread share at least one
	 * position in their respective current search paths.
	 */
	@Override
	protected boolean visit(final State state, final Position position) {
		final DualState dualState = (DualState) state;
		// track current position in set of all path positions
		dualState.pathAsSet.add(position);
		// add to path and check if done
		if (!super.visit(state, position)) {
			// not at target, does the other thread have us on its path?
			// if yes, abort search: we're done
			return dualState.other.pathAsSet.contains(position);
		}
		return true;
	}

	/**
	 * Removes the last position from the currently built search path,
	 * as well as from the set of positions making up the current search path.
	 * @param state the current search state
	 * @param idx the index of the last position in the search path
	 * @return the position removed from the path
	 */
	@Override
	protected Position backtrack(final State state, final int idx) {
		final Position result = super.backtrack(state, idx);
		((DualState) state).pathAsSet.remove(result);
		return result;
	}

	/**
	 * Extended version of the search state, which exposes a thread-safe
	 * set of the positions making up the currently built up path.
	 */
	protected static class DualState extends State {
		
		protected final Set<Position> pathAsSet;
		protected DualState other;
		
		/**
		 * Creates the state for the given maze and intended end position.
		 * @param sizeEstimate the estimated size of the result path
		 * @param maze the maze to search in
		 * @param exit the position to reach
		 */
		protected DualState(final int sizeEstimate, final Maze maze, final Position exit) {
			super(sizeEstimate, maze, exit);
			pathAsSet = Collections.newSetFromMap(new ConcurrentHashMap<>(4*sizeEstimate/3));
		}
	}

}
