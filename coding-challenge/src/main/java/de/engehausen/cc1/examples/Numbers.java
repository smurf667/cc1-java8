package de.engehausen.cc1.examples;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Contains sample number streams.
 */
public class Numbers {

	/**
	 * Returns a number stream with the consecutive numbers from
	 * one to ten. This stream contains the squares <code>[1, 4, 9]</code>.
	 * @return a small number stream, never <code>null</code>.
	 */
	public static Stream<Optional<? extends Number>> getSmallStream() {
		return IntStream
			.rangeClosed(1, 10)
			.boxed()
			.map(i -> Optional.of(i));
	}
	
	/**
	 * Returns a number stream with the consecutive numbers from
	 * one to 100000.
	 * @return a number stream, never <code>null</code>. The stream
	 * will be returned in <b>parallel</b> mode.
	 */
	public static Stream<Optional<? extends Number>> getLargeStream() {
		return IntStream
			.rangeClosed(1, 100000)
			.parallel()
			.boxed()
			.map(i -> Optional.of(i));
	}
}
