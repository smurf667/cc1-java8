package de.engehausen.cc1.impl.notused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.challenge.MazeSolver;

/**
 * A back-tracking, parallel maze solver.
 * <p><object type="image/svg+xml" data="../doc-files/maze64_parallel.svg" width="512" height="512"><param name="src" value="../doc-files/maze64_parallel.svg"></object></p>
 */
public class ParallelMazeSolver implements MazeSolver {

	private static final int NEIGHBOR_COUNT = Maze.Direction.values().length - 1; // aus einer richtung komme ich, ausser im startfall...

	private static final Maze.Direction[][] DIRECTIONS;
	
	static {
		final List<Maze.Direction[]> permutations = new PermutationGenerator<Maze.Direction>(Maze.Direction.values()).all();
		DIRECTIONS = permutations.toArray(new Maze.Direction[permutations.size()][]);
	}

	@Override
	public List<Position> getEscapeRoute(final Maze maze, final Position start, final Position exit) {
		final Forker forker = new Forker();
		final List<Future<List<Position>>> tasks = Collections.synchronizedList(new LinkedList<>());
		
		final Map<Position, List<Position>> candidates = new ConcurrentHashMap<>();
		try {
			final List<Position> path = new ArrayList<>();
			path.add(start);
			tasks.add(forker.submit(() -> {
				return search(maze, path, exit, candidates, forker, tasks);
			}));
			do {
				final List<Position> result = tasks.remove(0).get();
				if (result != null && !result.isEmpty()) {
					return result;
				}
			} while (!tasks.isEmpty());
			throw new IllegalStateException("no path found");
		} catch (InterruptedException | ExecutionException e) {
			throw new IllegalStateException(e);
		} finally {
			forker.shutdown();
		}
	}

	protected List<Position> search(final Maze maze, final List<Position> path, final Position exit, final Map<Position, List<Position>> candidates, final Forker forker, final List<Future<List<Position>>> tasks) {
		forker.notifyActive();
		try {
			Position position = path.get(path.size()-1);
			int olength = path.size(); // original length
			while (!exit.equals(position)) {
				if (forker.isActive() == false) {
					return Collections.emptyList();
				}
				List<Position> neighbors = candidates.get(position);
				if (neighbors == null) {
					neighbors = new ArrayList<>(NEIGHBOR_COUNT);
					// "randomly" choose a sequence of directions to go to
					for (Maze.Direction direction : DIRECTIONS[Math.abs(position.hashCode())%DIRECTIONS.length]) {
						if (maze.canGo(direction, position)) {
							final Position neighbor = position.neighborAt(direction);
							if (!candidates.containsKey(neighbor)) {
								neighbors.add(neighbor);
							}
						}
					}
					candidates.put(position, neighbors);
				}
				synchronized (neighbors) {
					int idx = neighbors.size() - 1;
					if (idx < 0) {
						final int last = path.size() - 1;
						path.remove(last); // dead end
						if (last == 0) {
							return null;
						} else {
							// try previous position
							position = path.get(last-1);
						}
					} else {
						final int currentSize = path.size();
						// more than one way to go, spawn parallel search if possible
						if (currentSize - olength > 16) { // only spawn if a couple of positions have been collected on the path
							while (idx > 0 && forker.canFork()) {
								final List<Position> newPath = new ArrayList<>(Math.max(32, currentSize + 32));
								newPath.addAll(path);
								newPath.add(neighbors.remove(idx--));
								tasks.add(forker.submit(() -> {
									return search(maze, newPath, exit, candidates, forker, tasks);
								}));
							}
							olength = currentSize; // original length
						}
						// go to the next possible position...
						path.add(position = neighbors.remove(idx));
					}
				}
			}
			try {
				return path;
			} finally {
				// signal success and try to abort other threads
				forker.stop();
			}
		} finally {
			forker.notifyDone();
		}
	}

	private static class Forker {
		
		private final AtomicInteger free;
		private final ExecutorService pool;
		
		private volatile boolean active;
		
		public Forker() {
			final int processorCount = Runtime.getRuntime().availableProcessors();
			free = new AtomicInteger(processorCount);
			pool = Executors.newWorkStealingPool(processorCount);
			active = true;
		}
		
		public boolean isActive() {
			return active;
		}
		
		public void stop() {
			free.set(0);
			active = false;
		}
		
		public boolean canFork() {
			return free.get() > 0;
		}
		
		public void notifyActive() {
			free.decrementAndGet();
		}
		
		public void notifyDone() {
			free.incrementAndGet();
		}

		public <T> Future<T> submit(final Callable<T> task) {
			return pool.submit(task);
		}

		public void shutdown() {
			active = false;
			pool.shutdownNow();
		}
	}
}
