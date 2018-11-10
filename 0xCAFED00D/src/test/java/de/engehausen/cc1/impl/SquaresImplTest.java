package de.engehausen.cc1.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import de.engehausen.cc1.examples.Numbers;

/**
 * Tests the square number filter.
 */
public class SquaresImplTest {

	@Test
	public void firstThreeSquares() {
		final List<Number> expected = Arrays.asList(
			Long.valueOf(1),
			Long.valueOf(4),
			Long.valueOf(9)
		);
		final List<Number> result = new SquaresImpl()
			.filterSquares(
				Numbers.getSmallStream()
			);
		Assert.assertEquals(expected, result);
	}

	@Test
	public void emptyOptional() {
		final List<Number> result = new SquaresImpl().filterSquares(
			Collections.<Optional<? extends Number>>singletonList(Optional.ofNullable(null))
				.stream()
			);
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void largeStream() {
		final List<Number> result = new SquaresImpl()
			.filterSquares(
				Numbers.getLargeStream()
			);
		Assert.assertEquals(316, result.size());
	}
}
