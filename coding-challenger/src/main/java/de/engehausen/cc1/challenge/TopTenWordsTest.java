package de.engehausen.cc1.challenge;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Test;

import de.engehausen.cc1.api.Word;
import de.engehausen.cc1.challenge.support.TestDescription;

@TestDescription(
	description="The challenge is to collect the top ten words of a word stream in lexicographical order."
)
public class TopTenWordsTest extends ReportingTest<TopTenWords> {
	
	/**
	 * The ten most common words from the King James Bible.
	 */
	public static final List<Word> TOP_TEN_BIBLE =
		Arrays
			.asList("THE", "AND", "OF", "TO", "THAT", "IN", "HE", "SHALL", "FOR", "UNTO")
			.stream()
			.map(str -> Word.from(str))
			.collect(Collectors.toList());

	public TopTenWordsTest() {
		super(TopTenWords.class);
	}

	@Test
	@TestDescription(
		description="There are no words to collect from the stream."
	)
	public void collectEmptyStream() {
		final List<Word> words = instance.getTopTenWords(Collections.<Word>emptyList().stream());
		Assert.assertNotNull(words);
		Assert.assertTrue("Word list is not empty", words.isEmpty());
	}

	@Test
	@TestDescription(
		description="All words in the stream are the same."
	)
	public void collectSameWords() {
		final Word testWord = Word.from("TEST");
		final List<Word> words = instance.getTopTenWords(
			Collections
				.nCopies(16, testWord)
				.stream()
		);
		Assert.assertNotNull(words);
		Assert.assertTrue("Result must contain exactly once the 'TEST' word.", words.size() == 1);
		Assert.assertEquals(testWord, words.get(0));
	}

	@Test
	@TestDescription(
		description="Stream supplies unique words in reverse lexicographical order."
	)
	public void verifyLexicographicalOrder() {
		final List<Word> words = instance.getTopTenWords(
			IntStream
				.rangeClosed(1, 25)
				.mapToObj(i -> Word.from(Character.toString((char) ('Z'-i))))
		);
		Assert.assertNotNull(words);
		Assert.assertTrue("Result words must have exactly ten entries", words.size() == 10);
		final StringBuilder sb = new StringBuilder(10);
		for (int i = 0; i < 10; i++) {
			sb.append(words.get(i));
		}
		Assert.assertEquals("ABCDEFGHIJ", sb.toString());
	}
	
	@Test
	@TestDescription(
		description="The <a href=\"http://www.gutenberg.org/ebooks/10\">King James Bible</a> from Project Gutenberg as a word stream.",
		performanceTest=true
	)
	public void kingJamesBible() throws IOException {
		// build the list of words in memory in order to build a
		// parallel stream for the test
		final List<Word> words = getWords();

		// measure with a parallel stream
		repeat(() -> {
			final List<Word> topTen = measure(
				() -> instance.getTopTenWords(words.parallelStream())
			);
			Assert.assertEquals(TOP_TEN_BIBLE, topTen);
		});

	}

	/**
	 * Reads the King James Bible words into a list.
	 * @return a word list, never <code>null</code>.
	 */
	protected List<Word> getWords() {
		final InputStream inputStream = getClass().getResourceAsStream("/pg10.txt");
		try {
			return StreamSupport.stream(
				new WordSpliterator(inputStream),
				false // sequential
			).collect(Collectors.toList());
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Naive word spliterator to build {@link Word} streams.
	 */
	private static class WordSpliterator implements Spliterator<Word> {
		
		private final InputStreamReader reader;
		private final StringBuilder builder;

		/**
		 * Creates the spliterator on the given input stream.
		 * 
		 * @param stream the stream to use, must not be <code>null</code>.
		 * The stream will be read in UTF-8 character set.
		 */
		public WordSpliterator(final InputStream stream) {
			reader = new InputStreamReader(stream, Charset.forName("UTF-8"));
			builder = new StringBuilder(32);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean tryAdvance(final Consumer<? super Word> action) {
			int c;
			try {
				while ( (c = reader.read()) != -1 ) {
					c = Word.getWordChar(c);
					if (c == 0) {
						if (builder.length() > 0) {
							break;
						}
					} else {
						builder.append((char) c);
					}
				}
				if (builder.length() > 0) {
					try {
						action.accept(Word.from(builder.toString()));
						return reader.ready();
					} finally {
						builder.setLength(0);
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Spliterator<Word> trySplit() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long estimateSize() {
			return 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int characteristics() {
			return NONNULL|IMMUTABLE;
		}
		
	}
}
