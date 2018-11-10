package de.engehausen.cc1.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.engehausen.cc1.challenge.Squares;

/**
 * A fast filter for square numbers.
 * Uses an algebraic shortcut to avoid some operations:
 * <a href="http://www.johndcook.com/blog/2008/11/17/fast-way-to-test-whether-a-number-is-a-square/">Fast way to test whether a number is a square</a>
 */
public class SquaresImpl implements Squares {

	private static final boolean SQUARE_POSSIBLE[];
	private static final int SQUARE_MASK;
	static {
		// compute shortcuts: build flags to indicate when
		// a number can't be a square, modulo 256
		SQUARE_POSSIBLE = new boolean[256];
		SQUARE_MASK = SQUARE_POSSIBLE.length-1;
		for (int n = 0; n <= SQUARE_MASK; n++) {
			SQUARE_POSSIBLE[n*n & SQUARE_MASK] = true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Number> filterSquares(final Stream<Optional<? extends Number>> numbers) {
		return Objects
			.requireNonNull(numbers) // ensures argument is not null
			.parallel() // process in parallel
			.mapToLong(optional -> optional.isPresent()?optional.get().longValue():Long.MIN_VALUE) // note: algorithm will fail for BigDecimals exceeding long range!
			.filter(n -> {
				if (n > 0) {
					if (SQUARE_POSSIBLE[(int) (n & SQUARE_MASK)]) {
						// check if this is a square number
						final long sqr = (long) Math.sqrt(n);
						return sqr*sqr == n;
					}
				}
				// this can't be a square number
				return false;
			})
			.mapToObj(n -> Long.valueOf(n)) // map...
			.collect(Collectors.toList()); // ...reduce
	}

}
