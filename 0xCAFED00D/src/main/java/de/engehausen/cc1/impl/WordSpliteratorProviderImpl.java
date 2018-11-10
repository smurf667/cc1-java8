package de.engehausen.cc1.impl;

import java.io.File;
import java.io.IOException;
import java.util.Spliterator;

import de.engehausen.cc1.api.Word;
import de.engehausen.cc1.challenge.WordSpliteratorProvider;

/**
 * Word spliterator provider implementation.
 * @see WordSpliterator
 */
public class WordSpliteratorProviderImpl implements WordSpliteratorProvider {

	/**
	 * {@inheritDoc}
	 * @throws IllegalStateException in case of error
	 */
	@Override
	public Spliterator<Word> getWordSpliterator(final File file) {
		try {
			// creates the spliterator with a 1MB threshold for splitting
			return new WordSpliterator(file, 1024*1024);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
