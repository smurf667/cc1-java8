package de.engehausen.cc1.impl.notused;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.engehausen.cc1.api.Maze.Direction;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.impl.RecursiveMazeSolver;

/**
 * Extension of the simple implementation (@link {@link RecursiveMazeSolver}.
 * This tries to prefer directions pointing toward the exit. This is slower than
 * the parent class. While seeming a good idea at first, the additional computations
 * cost time, and there is probably no argument for choosing any direction over any
 * other.
 */
public class RecursiveSearchStrategyMazeSolver extends RecursiveMazeSolver {
	
	private static Map<Direction, Direction[][]> SEARCH_DIRECTIONS = buildSearchDirections();

	protected Direction[] nextDirections(final Position current, final State state) {
		return SEARCH_DIRECTIONS
			.get(state.lastDirection)[toFlag(current, state.target)];
	}

	/**
	 * Returns a flag telling in which direction the target lies.
	 * Valid flags are:
	 * <ul>
	 * <li><code>0001</code> (1) - target is right, on this y</li>
	 * <li><code>0010</code> (2) - target is left, on this y</li>
	 * <li><code>0100</code> (4) - target is on this x, down</li>
	 * <li><code>0101</code> (5) - target is right, down</li>
	 * <li><code>0110</code> (6) - target is left, down</li>
	 * <li><code>1000</code> (8) - target is on this x, up</li>
	 * <li><code>1001</code> (9) - target is right, up</li>
	 * <li><code>1010</code> (10) - target is left, up</li>
	 * </ul>
	 * @param current the current position
	 * @param target the target position
	 * @return a flag value of {@link RecursiveSearchStrategyMazeSolver#VALID_DIRECTION_FLAGS}
	 */
	private static int toFlag(final Position current, final Position target) {
		return
			toFlag(target.getX() - current.getX()) |
			toFlag(target.getY() - current.getY())<<2;
	}
	
	/**
	 * Returns a flag component for the given delta.
	 * @param delta the delta between target and position
	 * @return zero if no delta, 1 if positive, 2 if negative
	 */
	private static int toFlag(final int delta) {
		if (delta == 0) {
			return 0;
		} else if (delta > 0) {
			return 1;
		}
		return 2;
	}

	private static Map<Direction, Direction[][]> buildSearchDirections() {
		final Position[] possibleLocations = {
				Position.at(1, 0),
				Position.at(-1, 0),
				Position.at(0, 1),
				Position.at(1, 1),
				Position.at(-1, 1),
				Position.at(0, -1),
				Position.at(1, -1),
				Position.at(-1, -1),
		};
		
		final Map<Direction, Direction[][]> result = new HashMap<>();
		result.put(null, buildSearchDirections(possibleLocations, createDirectionList(null)));
		for (Direction lastDirection : Direction.values()) {
			result.put(lastDirection, buildSearchDirections(possibleLocations, createDirectionList(lastDirection)));
		}
		return result;
	}
	
	private static Direction[][] buildSearchDirections(final Position[] possibleLocations, final List<Direction> nextDirections) {
		final Direction[][] result = new Direction[11][];
		final Position current = Position.at(0, 0);
		for (Position target : possibleLocations) {
			result[toFlag(current, target)] = orderDirections(current, target, nextDirections);
		}
		// this array is sparse; it will have null arrays for invalid flags
		return result;
	}

	private static Direction[] orderDirections(final Position current, final Position target, final List<Direction> nextDirections) {
		final List<Direction> result = new ArrayList<>(nextDirections);
		Collections.sort(result,
			(a, b) -> distance(target, current.neighborAt(a)) - distance(target, current.neighborAt(b))
		);
		return result.toArray(new Direction[nextDirections.size()]);
	}
	
	private static int distance(final Position first, final Position second) {
		final int dx = second.getX() - first.getX();
		final int dy = second.getY() - first.getY();
		return dx*dx + dy*dy;
	}

	private static List<Direction> createDirectionList(final Direction lastDirection) {
		final Direction drop = opposite(lastDirection);
		final List<Direction> result = new ArrayList<>();
		for (Direction direction : Direction.values()) {
			if (direction != drop) {
				result.add(direction);
			}
		}
		return result;
	}

	private static Direction opposite(final Direction direction) {
		if (direction == null) {
			return null;
		}
		switch (direction) {
			case LEFT:
				return Direction.RIGHT;
			case RIGHT:
				return Direction.LEFT;
			case UP:
				return Direction.DOWN;
			case DOWN:
				return Direction.UP;
			default:
				return direction;
		}
	}

	public static void main(String[] args) {
		final StringBuilder sb = new StringBuilder(8);
		for (Map.Entry<Direction, Direction[][]> entry : SEARCH_DIRECTIONS.entrySet()) {
			System.out.printf("lastDirection %s%n", entry.getKey());
			final Direction[][] dirs = entry.getValue();
			for (int i = 0; i < dirs.length; i++) {
				sb.setLength(0);
				sb.append(Integer.toBinaryString(i));
				while (sb.length() < 4) {
					sb.insert(0, '0');
				}
				if (dirs[i] != null) {
					System.out.printf("\t%s %s%n", sb.toString(), Arrays.asList(dirs[i]));
				}
			}
		}
	}
	
}
