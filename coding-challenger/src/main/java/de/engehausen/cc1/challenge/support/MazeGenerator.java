package de.engehausen.cc1.challenge.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;

/**
 * Generator for rectangular mazes. Uses
 * <a href="http://weblog.jamisbuck.org/2011/1/10/maze-generation-prim-s-algorithm">Prim's Algorithm</a> to
 * generate a maze. Mazes generated here do not have loops.
 */
public class MazeGenerator {

	private static MazeGenerator INSTANCE;

	/**
	 * Returns a maze generator instance.
	 * @return a maze generator instance, never <code>null</code>.
	 */
	public static MazeGenerator getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MazeGenerator();
		}
		return INSTANCE;
	}

	/**
	 * Returns a new maze instance with the given dimensions.
	 * The given number supplier is used to build the maze. The same maze is returned
	 * for the same input parameters always.
	 * @param width the width of the maze, in number of cells, a positive number.
	 * @param height the height of the maze, in number of cells, a positive number.
	 * @param randomNumberSupplier a supplier of "random" numbers, must not be <code>null</code>.
	 * @return a new maze instance, never <code>null</code>.
	 */
	public Maze getMaze(final int width, final int height, final IntSupplier randomNumberSupplier) {
		return new MazeImpl(width, height, randomNumberSupplier);
	}
	
	private static class MazeImpl implements Maze {

		private static final Predicate<Cell> IN_MAZE = c -> c.marked;
		private static final Predicate<Cell> NOT_IN_MAZE = c -> !c.marked;
		private final Cell[][] cells;
		
		protected MazeImpl(final int width, final int height, final IntSupplier ints) {
			final Cell[][] field = new Cell[height][];
			for (int y = 0; y < height; y++) {
				field[y] = new Cell[width];
				for (int x = 0; x < width; x++) {
					field[y][x] = new Cell(x, y);
				}
			}
			generate(field, ints);
			cells = field;
		}

		protected void generate(final Cell[][] field, final IntSupplier ints) {
			final Queue<Cell> frontier = new LinkedList<>();
			final int x = ints.getAsInt()%field[0].length;
			final int y = ints.getAsInt()%field.length;
			getCell(field, x, y).marked = true;
			addCells(field, x, y, frontier, NOT_IN_MAZE);
			Cell current;
			// list of adjacent cells
			final List<Cell> candidates = new ArrayList<>(Direction.values().length);
			while ((current = frontier.poll()) != null) {
				if (current.marked == false) {
					if ((ints.getAsInt() & 0x3) > 0) {
						mergeCells(field, ints, current, frontier, candidates);
					} else {
						frontier.add(current);
					}
				}
			}
		}

		protected void mergeCells(final Cell[][] field, final IntSupplier ints, final Cell cell, final Queue<Cell> frontier, final List<Cell> candidates) {
			addCells(field, cell.x, cell.y, candidates, IN_MAZE);
			// "randomly" pick a cell to merge
			mergeCell(cell, candidates.get(ints.getAsInt()%candidates.size()));
			candidates.clear();
			// add frontier cells of cell added to the maze
			addCells(field, cell.x, cell.y, frontier, NOT_IN_MAZE);
		}
		
		protected void mergeCell(final Cell cell, final Cell mazeCell) {
			final int dx = cell.x - mazeCell.x;
			if (dx != 0) {
				// left/right neighbor
				if (dx < 0) {
					cell.removeBorder(Direction.RIGHT);
					mazeCell.removeBorder(Direction.LEFT);
				} else {
					cell.removeBorder(Direction.LEFT);
					mazeCell.removeBorder(Direction.RIGHT);
				}
			} else {
				// up/down neighbor
				if (cell.y - mazeCell.y < 0) {
					cell.removeBorder(Direction.DOWN);
					mazeCell.removeBorder(Direction.UP);
				} else {
					cell.removeBorder(Direction.UP);
					mazeCell.removeBorder(Direction.DOWN);
				}
			}
			cell.marked = true;
		}

		protected void addCells(final Cell[][] field, final int x, final int y, final Collection<Cell> collection, final Predicate<Cell> predicate) {
			addCell(getCell(field, x, y-1), collection, predicate);
			addCell(getCell(field, x, y+1), collection, predicate);
			addCell(getCell(field, x-1, y), collection, predicate);
			addCell(getCell(field, x+1, y), collection, predicate);
		}

		protected void addCell(final Cell cell, final Collection<Cell> collection, final Predicate<Cell> predicate) {
			if (cell != null && predicate.test(cell)) {
				collection.add(cell);
			}
		}

		protected Cell getCell(final Cell[][] field, final int x, final int y) {
			if (y >= 0 && y < field.length) {
				if (x >= 0 && x < field[0].length) {
					return field[y][x];
				}
			}
			return null;
		}

		@Override
		public boolean canGo(final Direction direction, final Position position) {
			final int y = position.getY();
			if (y >= 0 && y < getHeight()) {
				final int x = position.getX();
				if (x >= 0 && x < getWidth()) {
					final Cell cell = cells[y][x];
					return !cell.hasBorder(direction);
				}
			}
			return false;
		}

		private int getWidth() {
			return cells[0].length;
		}

		private int getHeight() {
			return cells.length;
		}

	}

	private static class Cell {
		private final boolean noBorder[];
		protected final int x;
		protected final int y;
		protected boolean marked;
		protected Cell(final int x, final int y) {
			noBorder = new boolean[4];
			this.x = x;
			this.y = y;
		}
		boolean hasBorder(final Maze.Direction d) {
			return !noBorder[index(d)];
		}
		void removeBorder(final Maze.Direction d) {
			noBorder[index(d)] = true;
		}
		private int index(final Maze.Direction d) {
			switch (d) {
				case UP:
					return 0;
				case DOWN:
					return 1;
				case LEFT:
					return 2;
				case RIGHT:
					return 3;
				default:
					throw new IllegalArgumentException();
			}
		}
	}

}
