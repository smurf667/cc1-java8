package de.engehausen.cc1.api;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.engehausen.cc1.examples.Words;

/**
 * Representation of a word. A word is a string with characters
 * exclusively in the range from A to Z and has a minimal length of one character,
 * i.e. a word is a string that matches the regular expression <code>[A-Z]+</code>.
 * <p>A word can be obtained from its {@link String} representation using
 * the {@link #from(String)} method.</p>
 * <p>The class offers a helper that can efficiently map characters to
 * word characters, see {@link #getWordChar(int)}. This may be useful when
 * implementing a word stream.</p>
 * <p>You find example words in {@link Words}.</p>
 */
public class Word {

	/**
	 * A {@link Comparator} implementation for words.
	 */
	public static final Comparator<? super Word> COMPARATOR = (a,b) -> a.value.compareTo(b.value);

	/**
	 * Returns the word representation for the given word
	 * @param string a <b>valid</b> string representation of the word, i.e.
	 * a string matching the regular expression <code>[A-Z]+</code>.
	 * @return the word representation of the given string, never <code>null</code>.
	 * Please note: Illegal input results in illegal output unless you have assertions enabled.
	 */
	public static Word from(final String string) {
		Word result = cache.get(string);
		if (result == null) {
			result = new Word(string);
			cache.put(string, result);
		}
		return result;
	}

	/**
	 * Returns a word character for the given character value.
	 * @param c the character value.
	 * @return the word character, or the value 0 (zero) if not a word character.
	 */
	public static char getWordChar(final int c) {
		return WORD_CHARACTERS[c & 0x7f];
	}
	private static char WORD_CHARACTERS[];
	static {
		WORD_CHARACTERS = new char[128];
		for (int c = 0; c < WORD_CHARACTERS.length; c++) {
			if (c >= 'a' && c <= 'z') {
				WORD_CHARACTERS[c] = (char) (c - 32);
			} else if (c >= 'A' && c <= 'Z') {
				WORD_CHARACTERS[c] = (char) c;
			}
		}
	}

	// http://www.lingholic.com/how-many-words-do-i-need-to-know-the-955-rule-in-language-learning-part-2/
	private static Map<String, Word> cache = new ConcurrentHashMap<>(20000);//new WeakHashMap<>(20000);
	
	private final String value;

	/**
	 * Creates the word based on its string representation.
	 * @param string the string representing the word.
	 */
	private Word(final String string) {
		assert string != null;
		assert string.matches("[A-Z]+");
		value = string;
	}

	/**
	 * Returns the string representation of the word.
	 * @return the string representation of the word.
	 */
	@Override
	public String toString() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Word) {
			return value.equals(((Word) obj).value);
		}
		return false;
	}

}
