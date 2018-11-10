package de.engehausen.cc1.challenge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.challenge.support.MazeGenerator;
import de.engehausen.cc1.challenge.support.TestDescription;
import de.engehausen.cc1.examples.Mazes;

@TestDescription(
	description="The challenge is to a way out of a given maze."
)
public class MazeSolverTest extends ReportingTest<MazeSolver> {
	
	/**
	 * Creates the test for {@link MazeSolver} implementation.
	 */
	public MazeSolverTest() {
		super(MazeSolver.class);
	}

	@Test
	@TestDescription(
		description="Escape from the 12x6 example maze."
	)
	public void exampleMaze() throws Throwable {
		testMaze(
			Mazes.MAZE_12x6,
			instance.getEscapeRoute(Mazes.MAZE_12x6, Position.at(11, 5), Position.at(0, 0)),
			Position.at(11, 5),
			Position.at(0, 0));
	}

	@Test(expected=IllegalStateException.class)
	@TestDescription(
		description="No escape from a 1x1 maze with an unreachable exit position. This should throw an exception."
	)
	public void noEscape() throws Throwable {
		runWithTimeout(1000L, () -> {
			final Maze maze = MazeGenerator.getInstance().getMaze(1, 1, () -> 1);
			instance.getEscapeRoute(maze, Position.at(0, 0), Position.at(-1, 1));
		});
	}

	@Test
	@TestDescription(
		description="Escape from a 384x384 maze."
	)
	public void mediumMazeSize() throws Throwable {
		final Random rnd = new Random(0xbeef);
		final Maze maze = MazeGenerator.getInstance().getMaze(384, 384, () -> rnd.nextInt(383));
		runWithTimeout(() -> {
			testMaze(
				maze,
				instance.getEscapeRoute(maze, Position.at(0, 12), Position.at(0, 0)),
				Position.at(0, 12),
				Position.at(0, 0));
		});
	}

	@Test
	@TestDescription(
		description="Escape from a 2048x2048 maze.",
		performanceTest=true
	)
	public void largeMazeSize() throws Throwable {
		final Random rnd = new Random(0xbeef);
		final Maze maze = MazeGenerator.getInstance().getMaze(2048, 2048, () -> rnd.nextInt(1013));
		runWithTimeout(() -> {
			repeat(() -> {
				final List<Position> escape = measure(() ->
					instance.getEscapeRoute(maze, Position.at(0, 0), Position.at(2047, 2047))
				);
				testMaze(maze, escape, Position.at(0, 0), Position.at(2047, 2047));
			});
		});
	}

	protected void testMaze(final Maze maze, final List<Position> route, final Position start, final Position exit) {
		Assert.assertNotNull(route);
		Assert.assertTrue("route must have more than one position but was "+route, route.size() > 1);
		final Iterator<Position> i = route.iterator();
		Position current = i.next();
		while (i.hasNext()) {
			final Position next = i.next();
			if (!isNeighbor(current, next)) {
				Assert.fail("Non-neighbor found in escape route: " + current + " -> " + next);
			}
			if (!canGo(maze, current, next)) {
				Assert.fail("Illegal: there is a wall in the maze from " + current + " to " + next);
			}
			current = next;
		}
		Assert.assertEquals("Exit expected at last position of route", exit, current);
		Assert.assertTrue("No duplicate positions must exist", new HashSet<>(route).size() == route.size());
	}
	
	private boolean isNeighbor(final Position a, final Position b) {
		return (Math.abs(a.getX()-b.getX()) + Math.abs(a.getY()-b.getY())) == 1;
	}

	private boolean canGo(final Maze maze, final Position from, final Position to) {
		final int dx = to.getX() - from.getX();
		final Maze.Direction dir;
		if (dx == 0) {
			final int dy = to.getY() - from.getY();
			if (dy < 0) {
				dir = Maze.Direction.UP;
			} else {
				dir = Maze.Direction.DOWN;
			}
		} else {
			if (dx < 0) {
				dir = Maze.Direction.LEFT;
			} else {
				dir = Maze.Direction.RIGHT;
			}
		}
		return maze.canGo(dir, from);
	}

}
