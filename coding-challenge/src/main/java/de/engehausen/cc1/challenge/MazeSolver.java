package de.engehausen.cc1.challenge;

import java.util.List;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.examples.Mazes;

/**
 * Computes a route out of a {@link Maze}.
 * <p>You find example mazes in {@link Mazes}.</p>
 */
public interface MazeSolver {

	/**
	 * Returns a list of positions through the given maze, beginning with
	 * the starting position, and ending with the position of the given exit.
	 * @param maze the maze to escape from, must not be <code>null</code>.
	 * @param start the starting position of the escapee, must not be <code>null</code>.
	 * @param exit the exit position of the maze, must not be <code>null</code>.
	 * @return a list of <em>neighboring</em> positions that make up a route out of the maze, never <code>null</code>.
	 * No position in the list must occur more than once, i.e. the route must not have loops or
	 * any duplications.
	 * @throws IllegalStateException if no escape route can be found
	 */
	List<Position> getEscapeRoute(Maze maze, Position start, Position exit) throws IllegalStateException;

}
