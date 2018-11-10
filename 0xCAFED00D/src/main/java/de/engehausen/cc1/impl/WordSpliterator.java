package de.engehausen.cc1.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import de.engehausen.cc1.api.Word;

/**
 * A word spliterator implementation supporting parallelism and large files.
 * It supports splitting and parallel iteration and maps the file into memory using
 * {@link MappedByteBuffer}.
 */
public class WordSpliterator implements Spliterator<Word> {

	private final ByteBuffer buffer;
	private final StringBuilder contents;
	private final int splitThreshold;

	/**
	 * Creates the spliterator for the given file with an 8K splitting threshold.
	 * @param file the file for which to create the spliterator, must not be <code>null</code>.
	 * @throws IOException in case of error, e.g. file not found
	 */
	public WordSpliterator(final File file) throws IOException {
		this(file, 8192);
	}

	/**
	 * Creates the spliterator for the given file and splitting threshold.
	 * @param file the file for which to create the spliterator, must not be <code>null</code>.
	 * @param threshold the minimum size of remaining bytes in the file for which it is worthwile
	 * to split the iterator (for parallel processing).
	 * @throws IOException in case of error, e.g. file not found
	 */
	public WordSpliterator(final File file, final int threshold) throws IOException {
		Objects.nonNull(file);
		if (!file.exists()) {
			throw new IOException("Not found: "+file.getCanonicalPath());
		}
		splitThreshold = threshold;
		final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
		final FileChannel channel = randomAccessFile.getChannel();
		// fast, but comes with quite a drawback: http://bugs.java.com/view_bug.do?bug_id=4724038
		buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0L, channel.size());
		// from Channel JavaDoc: "A mapping, once established, is not dependent
		// upon the file channel that was used to create it. Closing the channel,
		// in particular, has no effect upon the validity of the mapping."
		channel.close();
		randomAccessFile.close();
		contents = new StringBuilder(64);
	}

	/**
	 * Copy constructor for splitting. This reuses the memory-mapped file and
	 * keeps track of the position in the buffer in regards to this instance
	 * of the splitted iterator.
	 * @param threshold the splitting threshold
	 * @param subBuffer the sub-buffer for the contents of this spliterator
	 */
	protected WordSpliterator(final int threshold, final ByteBuffer subBuffer) {
		buffer = subBuffer;
		splitThreshold = threshold;
		contents = new StringBuilder(64);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean tryAdvance(final Consumer<? super Word> wordConsumer) {
		// applies the next word of the stream to the consumer
		while (buffer.hasRemaining()) {
			final char i = Word.getWordChar(buffer.get());
			if (i > 0) {
				contents.append(i);
			} else if (contents.length() > 0) {
				wordConsumer.accept(Word.from(contents.toString()));
				contents.setLength(0);
				break;
			}
		}
		final boolean result = buffer.hasRemaining();
		if (result == false) {
			// all bytes read, but there's something in the internal
			// buffer, the last word...
			if (contents.length() > 0) {
				wordConsumer.accept(Word.from(contents.toString()));
			}
		}
		return result;
	}

	/**
	 * Tries to split the iterator. This is successful if the remaining
	 * bytes are more than the configured threshold. In this case the
	 * buffer is split in half, this instance continuing at the current
	 * position, the returned spliterator working on the other half.
	 * @return <code>null</code> if splitting is not possible, a word
	 * spliterator for the second half of the buffer otherwise.
	 */
	@Override
	public Spliterator<Word> trySplit() {
		final int remaining = buffer.limit() - buffer.position();
		if (remaining > splitThreshold) {
			final int move = remaining / 2;
			final ByteBuffer half = buffer.slice();
			half.position(move);
			// move to the beginning of a new word in the 2nd half buffer
			while (half.hasRemaining() && Word.getWordChar(half.get()) > 0);
			if (half.hasRemaining()) {
				// this buffer ends where the next begins
				buffer.limit(buffer.position() + half.position());
				return new WordSpliterator(splitThreshold, half);
			}
		}
		// no splitting possible
		return null;
	}

	/**
	 * Estimates the number of words in the spliterator.
	 * @return an estimate of the words in the spliterator.
	 */
	@Override
	public long estimateSize() {
		// English words seem to be 5 characters in length
		// add a little overhead like punctuation and other characters
		// see http://www.wolframalpha.com/input/?i=average+word+length+in+English
		return (buffer.limit() - buffer.position()) / 8;
	}

	/**
	 * Returns the characteristics of the spliterator.
	 * These are:
	 * <ul>
	 * <li>Returning immutable words,</li>
	 * <li>never returning <code>null</code> values, and</li>
	 * <li>"safe" for concurrent modification<sup>*</sup></li>
	 * </ul>
	 * <p>* No guarantees are made about changes made to the
	 * underlying file while iteration is in progress. It is
	 * expected that the iterated file is <em>never</em> modified
	 * while reading it!</p>
	 * @return the characteristics of the spliterator.
	 */
	@Override
	public int characteristics() {
		return NONNULL|IMMUTABLE|CONCURRENT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Comparator<? super Word> getComparator() {
		return Word.COMPARATOR;
	}

	// Please note: This implementation uses default methods for
	// - forEachRemaining(Consumer<? super Word>)
	// - getExactSizeIfKnown()
	// - hasCharacteristics(int)

}
