package de.engehausen.cc1.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.engehausen.cc1.api.Word;
import de.engehausen.cc1.examples.Words;

/**
 * Tests the collector for top ten words of a word stream.
 */
public class TopTenWordsImplTest {
	
	private TopTenWordsImpl impl;
	
	@Before
	public void setup() {
		impl = new TopTenWordsImpl();
	}
	
	@Test
	public void testLoreIpsum() {
		final List<Word> expected = Arrays.asList("IN", "UT", "DOLOR", "DOLORE", "AD", "ADIPISCING", "ALIQUA", "ALIQUIP", "AMET", "ANIM")
			.stream()
			.map(str -> Word.from(str))
			.collect(Collectors.toList());
		final List<Word> actual = impl.getTopTenWords(Words.getLoreIpsumStream());
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testSmall() {
		final List<Word> expected = Arrays.asList(
			Word.from("A"),
			Word.from("B"),
			Word.from("C")
		);
		final List<Word> actual = impl.getTopTenWords(expected.stream());
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testOne() {
		final List<Word> expected = Arrays.asList(
			Word.from("A")
		);
		final List<Word> actual = impl.getTopTenWords(expected.stream());
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testEmpty() {
		final List<Word> expected = Collections.emptyList();
		final List<Word> actual = impl.getTopTenWords(expected.stream());
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testAllTheSame() {
		final List<Word> expected = Collections.singletonList(Word.from("HELLO"));
		final List<Word> actual = impl.getTopTenWords(IntStream.range(0, 1000).boxed().map(i -> Word.from("HELLO")));
		Assert.assertEquals(expected, actual);
	}

}
