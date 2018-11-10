package de.engehausen.cc1.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.engehausen.cc1.api.Word;
import de.engehausen.cc1.challenge.TopTenWords;

/**
 * Producer for the top ten words of a word stream.
 */
public class TopTenWordsImpl implements TopTenWords {

	// representation of 'one', used as an immutable instance
	private static final AtomicInteger ONE = new AtomicInteger(1);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Word> getTopTenWords(final Stream<Word> wordStream) {
		// put all words into a map counting them
		final Map<Word, AtomicInteger> wordCounts = wordStream
			.parallel() // always work in parallel
			.collect(
				Collectors.<Word, Word, AtomicInteger>toMap( // http://stackoverflow.com/questions/22350288/parallel-streams-collectors-and-thread-safety
					word -> word, // the word is the key
					word -> ONE, // always map the key to immutable value 'one' first, but..
					(oldValue, delta) -> { // ...if a value already exists
						if (oldValue == ONE) {
							// ...create a merged, mutable version
							return new AtomicInteger(1+delta.get());
						}
						// ...otherwise update the mutable version
						oldValue.addAndGet(delta.get());
						return oldValue;
					}
				)
			);
		// use a specialized top ten list...
		final TopTenList<Map.Entry<Word, AtomicInteger>> topTen = 
			new TopTenList<>(
				count -> count.getValue().get(),
				(wordA, wordB) -> wordA.getKey().toString().compareTo(wordB.getKey().toString())
			);
		// ...and sequentially insert the words into it
		wordCounts
			.entrySet()
			.stream()
			.forEach(entry -> topTen.insert(entry));
		// return the top ten words, lexicographically ordered
		return topTen.getEntries(entry -> entry.getKey());
	}

	/**
	 * A specialized "top ten" linked list.
	 * @param <T> the type of elements the list holds
	 */
	private static class TopTenList<T> {

		private Node<T> first;
		private Node<T> last;
		private int count;
		private final ToIntFunction<T> getter;
		private final Comparator<T> comparator;

		/**
		 * Creates the top ten list.
		 * @param valueGetter function to retrieve the count of an element
		 * @param valueComparator comparator for values of the list
		 */
		public TopTenList(final ToIntFunction<T> valueGetter, final Comparator<T> valueComparator) {
			getter = valueGetter;
			comparator = valueComparator;
		}

		/**
		 * Inserts the given entry at the correct place in the top ten
		 * list.
		 * @param entry the entry to insert
		 */
		public void insert(final T entry) {
			final int value = getter.applyAsInt(entry);
			if (count > 0) {
				final int lastValue = getter.applyAsInt(last.value);
				if (lastValue >= value) {
					if (insertLast(entry, value, lastValue)) {
						return;
					}
				} else {
					Node<T> current = last.previous;
					while (current != null && getter.applyAsInt(current.value) < value) {
						current = current.previous;
					}
					if (current == null) {
						// insert at head
						insertFirst(entry);
					} else {
						// insert somewhere in the middle
						insertMiddle(current, entry, value);
					}
				}
				if (++count == 11) {
					// limit size to ten entries
					last = last.previous;
					last.next = null;
					count = 10;
				}
			} else {
				first = new Node<T>();
				first.value = entry;
				last = first;
				count = 1;
			}
		}

		protected void insertFirst(final T entry) {
			final Node<T> next = first;
			first = new Node<T>();
			first.value = entry;
			first.next = next;
			next.previous = first;
		}

		protected void insertMiddle(final Node<T> current, final T entry, final int value) {
			final Node<T> node = new Node<>();
			node.value = entry;
			node.previous = current;
			node.next = current.next;
			current.next.previous = node;
			current.next = node;
			lexiSort(node, value);
		}

		protected boolean insertLast(final T entry, final int value, final int lastValue) {
			if (count < 10) {
				final Node<T> old = last;
				last = new Node<T>();
				last.value = entry;
				last.previous = old;
				old.next = last;
				lexiSort(last, value);
				return false;
			} else {
				if (lastValue == value && comparator.compare(last.value, entry) > 0) {
					// the given entry is lexicographically smaller, replace the last node
					final Node<T> old = last.previous;
					last = old.next = new Node<T>();
					last.value = entry;
					last.previous = old;
					lexiSort(last, lastValue);
				}
				// too small, just drop
				return true;
			}
		}

		/**
		 * Ensures lexicographical sort order by moving the inserted node
		 * closer to the top for entries with the same value.
		 * @param insertedNode the inserted node
		 * @param value value of the inserted node
		 */
		protected void lexiSort(final Node<T> insertedNode, final int value) {
			Node<T> previous = insertedNode.previous;
			while (previous != null && getter.applyAsInt(previous.value) == value) {
				if (comparator.compare(previous.value, insertedNode.value) > 0) {
					swap(previous, insertedNode);
					previous = insertedNode.previous;
				} else {
					return;
				}
			}
		}

		/**
		 * Swaps predecessor and its successor
		 * @param predecessor
		 * @param successor
		 */
		protected void swap(final Node<T> predecessor, final Node<T> successor) {
			// update first/last if required
			if (predecessor == first) {
				first = successor;
			}
			if (successor == last) {
				last = predecessor;
			}
			if (successor.next != null) {
				successor.next.previous = predecessor;
			}
			if (predecessor.previous != null) {
				predecessor.previous.next = successor;
			}
			final Node<T> succNext = successor.next;
			successor.previous = predecessor.previous;
			successor.next = predecessor;
			predecessor.previous = successor;
			predecessor.next = succNext;
		}

		/**
		 * Returns the top ten words as a regular list.
		 * @param mapper a function to return the word from the internal container object
		 * @return the top ten words result list, never <code>null</code>.
		 */
		public <R> List<R> getEntries(final Function<T, R> mapper) {
			final List<R> result = new ArrayList<>(count);
			Node<T> current = first;
			while (current != null) {
				result.add(
					mapper.apply(current.value)
				);
				current = current.next;
			}
			return result;
		}

	}

	/**
	 * Linked list entry.
	 * @param <T> the type of object held by the entry.
	 */
	private static class Node<T> {
		Node<T> previous;
		Node<T> next;
		T value;
		public String toString() {
			return value.toString();
		}
	}

}
