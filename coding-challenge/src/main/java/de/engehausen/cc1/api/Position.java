package de.engehausen.cc1.api;

import de.engehausen.cc1.api.Maze.Direction;

/**
 * Two-dimensional position.
 * Positions can be obtained using the {@link #at(int, int)} method.
 * A position can also returns its neighboring position given a
 * direction, see {@link #neighborAt(Maze.Direction)}.
 */
public class Position {

	private static final int SHIFT = 10; // 2^10, see below
	// cache for the first million positions
	private static final Position[] CACHE = new Position[(2<<SHIFT)*(2<<SHIFT)];

	/**
	 * Returns a position object for the given coordinate.
	 * @param x the x component of the coordinate.
	 * @param y the y component of the coordinate.
	 * @return a position for the given coordinate, never <code>null</code>.
	 */
	public static Position at(final int x, final int y) {
		if (x < SHIFT && y < SHIFT) {
			final int offset = (y<<SHIFT) ^ x;
			if (offset >= 0) {
				final Position result = CACHE[offset];
				if (result == null) {
					return CACHE[offset] = new Position(x, y);
				}
			}
		}
		return new Position(x, y);
	}

	private int x;
	private int y;
	private final int hc;

	protected Position(final int x, final int y) {
		this.x = x;
		this.y = y;
		hc = ((x+1)*727)^((-y+1)*911);
	}

	/**
	 * Returns the x component of the position's coordinate.
	 * @return the x component of the position's coordinate.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the y component of the position's coordinate.
	 * @return the y component of the position's coordinate.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Returns the neighboring position for this position
	 * based on the given direction.
	 * @param direction the direction to which to return the
	 * neighbor for.
	 * @return the neighboring position for the given direction
	 * of this position, never <code>null</code>.
	 */
	public Position neighborAt(final Direction direction) {
		switch (direction) {
			case UP:
				return at(x, y-1);
			case DOWN:
				return at(x, y+1);
			case LEFT:
				return at(x-1, y);
			case RIGHT:
				return at(x+1, y);
			default:
				return this;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return hc;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Position) {
			final Position p = (Position) obj;
			return x == p.x && y == p.y;
		}
		return false;
	}
	
	/**
	 * A human-readable representation of the position.
	 * @return a human-readable representation of the position.
	 */
	public String toString() {
		final StringBuilder sb = new StringBuilder(12);
		sb.append(x).append(',').append(y);
		return sb.toString();
	}

}
