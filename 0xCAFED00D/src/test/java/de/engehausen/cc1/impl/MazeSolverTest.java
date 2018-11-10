package de.engehausen.cc1.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;
import de.engehausen.cc1.challenge.MazeSolver;
import de.engehausen.cc1.examples.Mazes;

/**
 * Base test for mazes. Apart from the actual maze solver
 * implementation used, there are a couple of alternative
 * implementations that can be tested with this test as
 * well.
 * @see de.engehausen.cc1.impl.notused
 */
public abstract class MazeSolverTest {

	protected MazeSolver solver;

	@Test
	public void test3x3() {
		testMaze(Mazes.MAZE_3x3, Position.at(2, 2), Mazes.MAZE_3x3_ROUTE);
	}

	@Test
	public void test7x7() {
		testMaze(Mazes.MAZE_7x7, Position.at(6, 6), Mazes.MAZE_7x7_ROUTE);
	}

	@Test
	public void test12x6() {
		testMaze(Mazes.MAZE_12x6, Position.at(11, 5), Mazes.MAZE_12x6_ROUTE);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testNoEscape() {
		solver.getEscapeRoute((a, b) -> false, Position.at(0, 0), Position.at(Integer.MAX_VALUE, Integer.MAX_VALUE));
	}

	@Test
	public void testAlreadyOut() {
		final List<Position> result = solver.getEscapeRoute((a, b) -> false, Position.at(0, 0), Position.at(0, 0));
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Position.at(0, 0), result.get(0));
	}

	protected void testMaze(final Maze maze, final Position exit, final List<Position> expected) {
		final List<Position> route = solver.getEscapeRoute(maze, Position.at(0, 0), exit);
		Assert.assertNotNull(route);
		Assert.assertTrue(route.size() > 1);
		final Iterator<Position> i = route.iterator();
		Position current = i.next();
		while (i.hasNext()) {
			final Position next = i.next();
			if (!isNeighbor(current, next)) {
				Assert.fail(current + " and " + next +" are not adjacent");
			}
			current = next;
		}
		Assert.assertEquals(new HashSet<>(expected), new HashSet<>(route));
	}
	
	private boolean isNeighbor(final Position a, final Position b) {
		return (Math.abs(a.getX()-b.getX()) + Math.abs(a.getY()-b.getY())) == 1;
	}
}
