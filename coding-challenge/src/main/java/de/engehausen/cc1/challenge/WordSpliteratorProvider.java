package de.engehausen.cc1.challenge;

import java.io.File;
import java.util.Spliterator;

import de.engehausen.cc1.api.Word;

/**
 * Provides a spliterator for {@link Word}s in a file.
 */
public interface WordSpliteratorProvider {

	/**
	 * Returns a spliterator of words for the given file.
	 * @param file the file to create a spliterator for, never <code>null</code>.
	 * The file contents must be in <code>US-ASCII</code> encoding.
	 * @return a spliterator of words for the given file, never <code>null</code>.
	 */
	Spliterator<Word> getWordSpliterator(File file);

}
