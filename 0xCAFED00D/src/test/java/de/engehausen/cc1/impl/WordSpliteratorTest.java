package de.engehausen.cc1.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Test;

import de.engehausen.cc1.api.Word;
import de.engehausen.cc1.examples.Words;

/**
 * Tests the word spliterator implementation.
 */
public class WordSpliteratorTest {

	private static final String PREFIX = "spliterator-";
	private static final String SUFFIX = ".txt";

	@Test
	public void testSimple() throws IOException {
		final File tempFile = createWordFile(1);
		try {
			final WordSpliterator spliterator = new WordSpliterator(tempFile);
					Assert.assertTrue(spliterator.estimateSize() > 0);
					Assert.assertEquals(Spliterator.NONNULL|Spliterator.IMMUTABLE|Spliterator.CONCURRENT, spliterator.characteristics());
					Words.getLoreIpsumStream().forEach(
						expected -> spliterator.tryAdvance(
							actual -> Assert.assertEquals(expected, actual)
						)
					);
					Assert.assertFalse(spliterator.tryAdvance(w -> { /*ignore*/}));
					Assert.assertNotNull(spliterator.getComparator());
		} finally {
			deleteFile(tempFile);
		}
	}
	
	/**
	 * Reads the "lore ipsum" words from variously sized streams and
	 * ensures that the exact same words are encountered, no matter how
	 * big the stream is.
	 * @throws IOException in case of error
	 */
	@Test
	public void testHuge() throws IOException {
		final Set<Word> expected = Words.getLoreIpsumStream().collect(Collectors.toSet());
		Assert.assertEquals(expected, collect(1, false));
		Assert.assertEquals(expected, collect(200000, true));
	}
	
	/**
	 * Creates a temporary file repeating the "lore ipsum" words
	 * the given number of time.
	 * @param repeats repetition count of the lore ipsum words.
	 * @return the temporary file, never <code>null</code>
	 * @throws IOException in case of error
	 */
	protected File createWordFile(final int repeats) throws IOException {
		final File result = File.createTempFile(PREFIX, SUFFIX);
		final FileOutputStream fos = new FileOutputStream(result.getCanonicalFile());
		final byte[] bytes = (Words.LORE_IPSUM + "\n").getBytes(Charset.forName("US-ASCII"));
		try {
			for (int i = 0; i < repeats; i++) {
				fos.write(bytes);
			}
		} finally {
			fos.close();
		}
		return result;
	}

	/**
	 * Collect the words from a temporary file that repeats the "lore ipsum"
	 * words the given number of times.
	 * @param repeats repetition count of the lore ipsum words.
	 * @param parallel if the spliterator should operate in parallel mode or not
	 * @return the collected words, never <code>null</code>
	 * @throws IOException in case of error
	 */
	protected Set<Word> collect(final int repeats, final boolean parallel) throws IOException {
		final File f = createWordFile(repeats);
		try {
			return StreamSupport.stream(new WordSpliterator(f), parallel)
				.collect(
					Collectors.toSet()
				);
		} finally {
			deleteFile(f);
		}
	}

	/**
	 * Tries to delete the given file.
	 * @param f the file to delete, may be <code>null</code>.
	 */
	protected void deleteFile(final File f) {
		if (f != null) {
			// Java bug about open memory mapped files :-( http://bugs.java.com/view_bug.do?bug_id=4724038
			System.gc();
			Thread.yield();
			if (!f.delete()) {
				f.deleteOnExit();
			}
		}
	}
	
}
