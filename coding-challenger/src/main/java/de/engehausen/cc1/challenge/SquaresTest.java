package de.engehausen.cc1.challenge;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import de.engehausen.cc1.challenge.support.TestDescription;

@TestDescription(
	description="The challenge is to filter out any non-square numbers."
)
public class SquaresTest extends ReportingTest<Squares> {

	/**
	 * Creates the test for {@link Squares} implementation.
	 */
	public SquaresTest() {
		super(Squares.class);
	}
	
	@Test
	@TestDescription(
		description="There are actually no numbers in the stream to filter."
	)
	public void filterEmptyStream() {
		final List<? extends Number> result = instance.filterSquares(
			IntStream
				.empty()
				.boxed()
				.map(i -> Optional.of(i))
			);
		Assert.assertNotNull("The result list is null.", result);
		Assert.assertTrue("The result list is not empty.", result.isEmpty());
	}

	@Test
	@TestDescription(
			description="The stream does not contain any squares."
		)
	public void filterNoSquaresStream() {
		final List<? extends Number> result = instance.filterSquares(
			Collections
				.<Optional<? extends Number>>nCopies(32, Optional.of(Integer.valueOf(3)))
				.stream()
			);
		Assert.assertNotNull("The result list is null", result);
		Assert.assertEquals("The result list is not empty.", Collections.emptyList(), result);
	}

	@Test
	@TestDescription(
		description="Small stream of integers from 1 to 100 with a few squares."
	)
	public void filterSmallIntegerStream() {
		final List<? extends Number> result = instance.filterSquares(
			IntStream
				.rangeClosed(1, 100)
				.boxed()
				.map(i -> Optional.of(i))
			);
		verifyFirstHundred(result);
	}

	@Test
	@TestDescription(
		description="Stream with mixed number types such as <code>java.lang.Long</code>, <code>java.lang.Double</code> and <code>java.lang.BigDecimal</code>. Also contains empty optionals."
	)
	public void filterMixedStream() {
		final List<Optional<? extends Number>> input = new ArrayList<>();
		IntStream
			.rangeClosed(1, 100)
			.boxed()
			.map(i -> {
				switch (i.intValue() % 3) {
					case 0:
						return Optional.of(new BigDecimal(i.intValue()));
					case 1:
						return Optional.of(Long.valueOf(i.intValue()));
					case 2:
						return Optional.of(Double.valueOf(i.intValue()));
					default:
						// this won't happen, but without it the compiler is unhappy
						return Optional.of(i);
				}
			})
			.forEach(v -> input.add(v));
		input.add(Optional.empty());
		// shuffle into a predictable but non-linear order
		Collections.shuffle(input, new Random(0x55555555));
		verifyFirstHundred(
			instance.filterSquares(
				input.stream()
			)
		);
	}

	@Test
	@TestDescription(
		description="An integer stream for all squares between 1 and one hundred million.",
		performanceTest=true
	)
	public void filterOneHundredMillion() {
		repeat(() -> {
			final List<? extends Number> result = measure(() -> instance.filterSquares(
				IntStream
					.rangeClosed(1, 100000000)
					.boxed()
					.map(num -> Optional.of(num))
				)
			);
			Assert.assertNotNull("The result list is null", result);
			Assert.assertEquals("Result list did not contain expected number of elements.", 10000, result.size());
		});
	}

	private void verifyFirstHundred(final List<? extends Number> result) {
		Assert.assertNotNull("The result list is null", result);
		Assert.assertEquals("Unexpected number of squares", 10, result.size());
		// create a set of integers from the result numbers
		final Set<Integer> numbers = result.stream()
			.map(i -> Integer.valueOf(i.intValue()) )
			.collect(Collectors.toSet());
		// produce all squares in 1..100 and remove from set
		IntStream
			.rangeClosed(1, 10)
			.boxed()
			.map(i -> Integer.valueOf(i.intValue()*i.intValue()))
			.forEach(i -> numbers.remove(i));
		// the numbers set must be empty
		Assert.assertTrue("The result contains invalid entries: "+numbers, numbers.isEmpty());
	}
}
