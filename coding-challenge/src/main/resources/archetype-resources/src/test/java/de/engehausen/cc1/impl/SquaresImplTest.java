package de.engehausen.cc1.impl;

import java.util.Arrays;
import java.util.List;

import de.engehausen.cc1.examples.Numbers;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link de.engehausen.cc1.impl.SquaresImpl}.
 * TODO it is good practice to test your code
 */
public class SquaresImplTest {

	/**
	 * Tests collecting all squares in the interval <code>[1,10]</code>.
	 */
	@Test
	public void simpleTest() {
		final List<Number> expected = Arrays.asList(
			Integer.valueOf(1),
			Integer.valueOf(4),
			Integer.valueOf(9)
		);
		final List<Number> result = new SquaresImpl().filterSquares(Numbers.getSmallStream());
// TODO enable below assertion
//		Assert.assertEquals(expected, result);
	}

}
