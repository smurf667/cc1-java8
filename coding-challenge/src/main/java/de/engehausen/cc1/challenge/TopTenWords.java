package de.engehausen.cc1.challenge;

import java.util.List;
import java.util.stream.Stream;

import de.engehausen.cc1.api.Word;
import de.engehausen.cc1.examples.Words;

/**
 * Obtains the top ten words of a {@link Word} stream.
 * <p>You find example words in {@link Words}.</p>
 */
public interface TopTenWords {

	/**
	 * Returns the top ten words in descending order.
	 * The order is defined by number of occurrences and
	 * then by lexicographical order in case two or more words
	 * have the same occurrence count.
	 * @param wordStream the stream of words to process
	 * @return a list with the top ten words of the stream, never <code>null</code>.
	 */
	List<Word> getTopTenWords(Stream<Word> wordStream);

}
