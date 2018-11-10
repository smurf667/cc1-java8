package de.engehausen.cc1.impl.notused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;

/**
 * Two-threaded maze solver using a shared visited map which allows
 * to reconstruct the path the moment both threads meet on that map.
 * Theoretically at least for large mazes this should be faster than
 * a single-threaded version, but during tests it showed it rarely was.
 * Often, it was slower.
 */
public class DualThreadRecursiveMazeSolver2 extends RecursiveMazeSolver2 {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Position> getEscapeRoute(final Maze maze, final Position start, final Position exit) throws IllegalStateException {
		final int estimatedSize = estimateResultPathSize(maze, start, exit);
		final Set<Position> allVisited = Collections.newSetFromMap(new ConcurrentHashMap<>(4*estimatedSize/3));
		final DualState forwardState = new DualState(estimatedSize, maze, exit, allVisited);
		final DualState backwardState = new DualState(estimatedSize, maze, start, allVisited);
		final Thread forwardWorker = new Thread(() -> super.recursiveSearch(forwardState, start));
		forwardWorker.start();
		final Thread backwardWorker = new Thread(() -> super.recursiveSearch(backwardState, exit));
		backwardWorker.start();
		try {
			forwardWorker.join();
			backwardWorker.join();
		} catch (InterruptedException e) {
			// give up, search single-threaded
			return super.getEscapeRoute(maze, start, exit);
		}
		if (forwardState.last.equals(forwardState.visited.get(exit))) {
			// the forward searcher has found the complete route
			return buildPath(forwardState, exit);
		}
		if (backwardState.last.equals(backwardState.visited.get(start))) {
			// the backward searcher has found the complete route
			final ArrayList<Position> result = new ArrayList<>(buildPath(backwardState, start));
			Collections.reverse(result);
			return result;
		}
		final Position commonPosition;
		if (forwardState.visited.containsKey(backwardState.last)) {
			commonPosition = backwardState.last;
		} else if (backwardState.visited.containsKey(forwardState.last)) {
			commonPosition = forwardState.last;
		} else {
			throw new IllegalStateException("no route found");
		}
		final List<Position> forwardPath = buildPath(forwardState, commonPosition);
		final List<Position> backwardPath = new ArrayList<>(buildPath(backwardState, commonPosition));
		backwardPath.remove(backwardPath.size()-1);
		Collections.reverse(backwardPath);
		final List<Position> result = new ArrayList<>(forwardPath.size()+backwardPath.size());
		result.addAll(forwardPath);
		result.addAll(backwardPath);
		return result;
	}

	@Override
	protected boolean visit(final State state, final Position position) {
//		if (position.getY()%3==0) Thread.yield();
		final boolean result = super.visit(state, position);
		final DualState dualState = (DualState) state;
		if (!dualState.marked.add(position)) {
			// the other party was here already, abort search as
			// the full path can be reconstructed now
			return true;
		}
		return result;
	}

	public static class DualState extends State {
		
		public final Set<Position> marked;
		
		public DualState(final int sizeEstimate, final Maze maze, final Position exit, final Set<Position> sharedPositions) {
			super(sizeEstimate, maze, exit);
			marked = sharedPositions;
		}
	}

}
