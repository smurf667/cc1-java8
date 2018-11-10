package de.engehausen.cc1.examples;

import java.util.Arrays;
import java.util.List;

import de.engehausen.cc1.api.Maze;
import de.engehausen.cc1.api.Position;

/**
 * Contains example mazes.
 * <p><img src="doc-files/mazes.png" alt="visual representation"></p>
 */
public class Mazes {
	
	/** 3x3 example maze */
	public static final Maze MAZE_3x3 = new MazeImpl(new int[][] {
		{ 0xc, 0xa, 0x6 },
		{ 0xd, 0x6, 0x5 },
		{ 0x1, 0x1, 0x1 }
	});
	/** example route from (0,0) to (2,2) */
	public static final List<Position> MAZE_3x3_ROUTE = Arrays.asList(Position.at(0, 0), Position.at(1, 0),
		Position.at(2, 0), Position.at(2, 1), Position.at(2, 2));

	/** 7x7 example maze */
	public static final Maze MAZE_7x7 = new MazeImpl(new int[][] {
		{ 0x8, 0xa, 0xa, 0x6, 0x8, 0x6, 0x4 },
		{ 0xc, 0xa, 0xe, 0x7, 0x8, 0xb, 0x7 },
		{ 0x1, 0x8, 0x3, 0xd, 0xa, 0xe, 0x7 },
		{ 0xc, 0xa, 0xa, 0x7, 0xc, 0x7, 0x5 },
		{ 0x5, 0xc, 0xe, 0x3, 0x5, 0x1, 0x5 },
		{ 0x5, 0x1, 0x5, 0x8, 0x3, 0xc, 0x7 },
		{ 0x1, 0x8, 0x3, 0x8, 0xa, 0x3, 0x1 }
	});
	/** example route from (0,0) to (6,6) */
	public static final List<Position> MAZE_7x7_ROUTE = Arrays.asList(Position.at(0, 0), Position.at(1, 0),
		Position.at(2, 0), Position.at(3, 0), Position.at(3, 1), Position.at(3, 2), Position.at(4, 2),
		Position.at(5, 2), Position.at(6, 2), Position.at(6, 3), Position.at(6, 4), Position.at(6, 5),
		Position.at(6, 6));

	/** 12x6 example maze */
	public static final Maze MAZE_12x6 = new MazeImpl(new int[][] {
		{ 0xc, 0xe, 0xe, 0xe, 0xa, 0x2, 0x4, 0xc, 0xa, 0x2, 0x4, 0x4 },
		{ 0x1, 0x1, 0x5, 0xd, 0xe, 0xe, 0xf, 0xf, 0xe, 0xa, 0xf, 0x3 },
		{ 0xc, 0xe, 0x7, 0x5, 0x1, 0x5, 0x1, 0x5, 0x9, 0x6, 0xd, 0x6 },
		{ 0x5, 0x1, 0x5, 0x9, 0x6, 0x9, 0x6, 0x5, 0x4, 0x5, 0x5, 0x5 },
		{ 0x5, 0xc, 0xf, 0x2, 0xd, 0x6, 0x5, 0xd, 0x3, 0x5, 0x1, 0x5 },
		{ 0x1, 0x1, 0x1, 0x8, 0x3, 0x1, 0x1, 0x9, 0x2, 0x9, 0x2, 0x1 }
	});
	/** example route from (0,0) to (11,5) */
	public static final List<Position> MAZE_12x6_ROUTE = Arrays.asList(Position.at(0, 0), Position.at(1, 0),
		Position.at(2, 0), Position.at(3, 0), Position.at(3, 1), Position.at(4, 1), Position.at(5, 1),
		Position.at(6, 1), Position.at(7, 1), Position.at(8, 1), Position.at(9, 1), Position.at(10, 1),
		Position.at(10, 2), Position.at(11, 2), Position.at(11, 3), Position.at(11, 4), Position.at(11, 5));

	private static class MazeImpl implements Maze {
		
		private final int[][] flags;
		private final int width;
		private final int height;
		
		private MazeImpl(final int[][] field) {
			flags = field;
			height = field.length;
			width = field[0].length;
		}

		@Override
		public boolean canGo(final Direction direction, final Position position) {
			final int x = position.getX();
			if (x >= 0 && x < width) {
				final int y = position.getY();
				if (y >= 0 && y < height) {
					return (flags[y][x] & directionToFlag(direction)) != 0;
				}
				
			}
			return false;
		}
		
		private int directionToFlag(final Direction direction) {
			switch (direction) {
				case UP:
					return 1;
				case LEFT:
					return 2;
				case DOWN:
					return 4;
				case RIGHT:
					return 8;
				default:
					return 0;
			}
		}

	}
}
