package de.engehausen.cc1.api;

import de.engehausen.cc1.examples.Mazes;

/**
 * A maze. The only information offered is if it is possible
 * to move to another position in a certain direction given a position,
 * see {@link #canGo(Direction, Position)}.
 * <p>You find example mazes in {@link Mazes}.</p>
 */
public interface Maze {

	/**
	 * Movement directions.
	 */
	enum Direction {
		UP, LEFT, DOWN, RIGHT;
	};

	/**
	 * Indicates whether it is possible to move into the given
	 * direction for the given position.
	 * @param direction the direction to go into, must not be <code>null</code>.
	 * @param position the position to check, must not be <code>null</code>.
	 * @return <code>true</code> if it is possible to move into the
	 * given direction, <code>false</code> otherwise.
	 */
	boolean canGo(Direction direction, Position position);

}
