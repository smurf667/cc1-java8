package de.engehausen.cc1.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.engehausen.cc1.api.Word;

/**
 * Contains sample words.
 */
public class Words {

	/**
	 * "Lore ipsum", a classical test text, see <a href="https://en.wikipedia.org/wiki/Lorem_ipsum">Wikipedia</a>.
	 */
	public static String LORE_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	private static List<Word> LORE_IPSUM_WORDS;

	/**
	 * Returns a stream of {@link Word} objects.
	 * The top ten words in lexicographical order are:
	 * <ul>
	 * <li>IN (3)</li>
	 * <li>UT (3)</li>
	 * <li>DOLOR (2)</li>
	 * <li>DOLORE (2)</li>
	 * <li>AD (1)</li>
	 * <li>ADIPISCING (1)</li>
	 * <li>ALIQUA (1)</li>
	 * <li>ALIQUIP (1)</li>
	 * <li>AMET (1)</li>
	 * <li>ANIM (1)</li>
	 * </ul>
	 * @return a stream of words, never <code>null</code>.
	 */
	public static Stream<Word> getLoreIpsumStream() {
		if (LORE_IPSUM_WORDS == null) {
			LORE_IPSUM_WORDS = new ArrayList<>();
			final String[] strings = LORE_IPSUM
				.replaceAll("[\\.,]", "")
				.toUpperCase()
				.split(" ");
			for (String string : strings) {
				LORE_IPSUM_WORDS.add(Word.from(string));
			}
		}
		return LORE_IPSUM_WORDS.stream();
	}
}
