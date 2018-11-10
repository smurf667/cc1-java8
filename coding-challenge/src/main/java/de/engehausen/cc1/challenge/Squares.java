package de.engehausen.cc1.challenge;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.engehausen.cc1.examples.Numbers;

/**
 * Provides a list of square numbers.
 * <p>You find example number streams in {@link Numbers}.</p>
 */
public interface Squares {

	/**
	 * Returns all numbers that are squares from the given
	 * number stream.
	 * @param numbers a stream of numbers, never <code>null</code>.
	 * The numbers of the stream must be non-zero and positive.
	 * @return a list that contains all squares of the number stream, never <code>null</code>.
	 */
	List<Number> filterSquares(Stream<Optional<? extends Number>> numbers);

}
