package de.engehausen.cc1.impl;

import java.io.File;
import java.io.IOException;
import java.util.Spliterator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.engehausen.cc1.api.Word;

/**
 * Tests the word spliterator provider implementation.
 * Just very basic tests, as the actual functionality is
 * tested in {@link WordSpliteratorTest}.
 */
public class WordSpliteratorProviderImplTest {
	
	private File file;

	@Before
	public void setup() throws IOException {
		file = File.createTempFile("spliteratorprovider-", ".txt");
	}
	
	@After
	public void shutdown() {
		if (file != null) {
			if (!file.delete()) {
				file.deleteOnExit();
			}
		}
	}
	
	@Test
	public void testSimple() {
		final Spliterator<Word> spliterator = new WordSpliteratorProviderImpl().getWordSpliterator(file);
		Assert.assertNotNull(spliterator);
		while (spliterator.tryAdvance( w -> {/*ignore*/} ));
	}

	@Test(expected=IllegalStateException.class)
	public void testFileDoesNotExist() {
		new WordSpliteratorProviderImpl().getWordSpliterator(new File("DOES-NOT-EXIST"));
	}

}
