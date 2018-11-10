package de.engehausen.cc1.challenge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Test;

import de.engehausen.cc1.api.Word;
import de.engehausen.cc1.challenge.support.TestDescription;

@TestDescription(
	description="The challenge is to implement a <code>Spliterator</code> for words. Optional properties of this interface (e.g. supporting parallelism) make this a bigger challenge than it might seem on first glance."
)
public class WordSpliteratorProviderTest extends ReportingTest<WordSpliteratorProvider>{

	private static final int REPEAT = 100;
	private final File tempDir;
	
	public WordSpliteratorProviderTest() {
		super(WordSpliteratorProvider.class);
		tempDir = new File(System.getProperty("java.io.tmpdir"));
	}

	@Test
	@TestDescription(
		description="A stream of words from the beginning of the Wikipedia article on computer science."
	)
	public void wikiAbstract() throws IOException {
		final File wikiText = ensureFilePresent("wiki.txt", "/wiki.txt", 1);
		final Spliterator<Word> spliterator = instance.getWordSpliterator(wikiText);
		Assert.assertNotNull("spliterator is null", spliterator);
		final List<Word> words = StreamSupport
			.stream(spliterator, false)
			.collect(Collectors.toList());
		final List<Word> expected = Arrays.asList(
			"COMPUTER", "SCIENCE", "FROM", "WIKIPEDIA", "THE", "FREE", "ENCYCLOPEDIA", "COMPUTER", "SCIENCE", "IS"
		).stream()
			.map(str -> Word.from(str))
			.collect(Collectors.toList());
		Assert.assertEquals("Incorrect beginning words", expected, words.subList(0, Math.min(10, words.size())));
		Assert.assertEquals("Unexpected last word", Word.from("HUMANS"), words.get(words.size()-1));
		Assert.assertEquals("Expected 187 words", 187, words.size());
	}

	@Test
	@TestDescription(
		description="Huge word stream (King James Bible repeated a hundred times, ca. 0.5GB).",
		performanceTest=true
	)
	public void countBibleWords() throws Throwable {
		final File largeBible = ensureFilePresent("bible_x_100.txt", "/pg10.txt", REPEAT);
		final Properties bibleWords = new Properties();
		bibleWords.load(getClass().getResourceAsStream("/pg10.properties"));
		final int wordCount = REPEAT*bibleWords.values()
			.stream()
			.mapToInt(v -> Integer.parseInt(v.toString()))
			.sum();
		final AtomicInteger immutableOne = new AtomicInteger(1);
		runWithTimeout(() -> {
			repeat(() -> {
				final Map<Word, AtomicInteger> words = measure(() -> {
					final Spliterator<Word> spliterator = instance.getWordSpliterator(largeBible);
					Assert.assertNotNull(spliterator);
					return StreamSupport
						.stream(spliterator, true) // try a parallel stream
						.collect(
							Collectors.<Word, Word, AtomicInteger>toConcurrentMap(
								word -> word, // the word is the key
								word -> immutableOne, // always map the key to immutable value 'one' first, but..
								(oldValue, newValue) -> { // ...if a value already exists
									if (oldValue == immutableOne) {
										// ...create a merged, mutable version
										return new AtomicInteger(1+newValue.get());
									}
									// ...otherwise update the mutable version
									oldValue.addAndGet(newValue.get());
									return oldValue;
								}
							)
						);
				});
				Assert.assertEquals("Unexpected number of unique words", bibleWords.size(), words.size());
				Assert.assertEquals("Unexpected number of words", wordCount, words
					.values()
					.stream()
					.mapToInt(v -> v.get())
					.sum()
				);
				verifyCount(Word.from("DRUNKARDS"), words, 6*REPEAT);
				verifyCount(Word.from("LIZARD"), words, 1*REPEAT);
			});
		});
	}

	@Test
	@TestDescription(
		description="Finding a word in a huge word stream using <code>findAny</code>.",
		performanceTest=true
	)
	public void findWord() throws Throwable {
		runWithTimeout(() -> {
			try {
				final File largeBible = ensureFilePresent("bible.txt", "/pg10.txt", 1);
				final Word toFind = Word.from("AGONY"); // amazingly, occurs just once in the text
				repeat(() -> {
					final Optional<Word> result = measure( () -> {
						final Spliterator<Word> spliterator = instance.getWordSpliterator(largeBible);
						Assert.assertNotNull("spliterator is null", spliterator);
						return StreamSupport
							.stream(spliterator, true)
							.filter(w -> toFind.equals(w) )
							.findAny();
					});
					Assert.assertTrue("Word AGONY not found", result.isPresent());
					Assert.assertEquals(toFind, result.get());
				});
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		});
	}

	protected void verifyCount(final Word key, final Map<Word, AtomicInteger> words, final int count) {
		final AtomicInteger integer = words.get(key);
		final String word = key.toString();
		Assert.assertNotNull("Word not found: "+word, integer);
		Assert.assertEquals("Unexpected count for "+word, count, integer.intValue());
	}

	protected File ensureFilePresent(final String name, final String resource, final int repeats) throws IOException {
		final File result = new File(tempDir, name);
		if (!result.exists()) {
			for (int i = 0; i < repeats; i++) {
				final InputStream source = getClass().getResourceAsStream(resource);
				try {
					final FileOutputStream fos = new FileOutputStream(result, true);
					try {
						final byte[] buffer = new byte[65536];
						int len;
						while ((len = source.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					} finally {
						fos.close();
					}
				} finally {
					source.close();
				}
			}
		}
		return result;
	}

}
