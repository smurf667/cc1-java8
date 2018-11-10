package de.engehausen.cc1.impl.notused;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for an array of objects.
 * This is used in the maze solvers to use different movement sequences
 * so not to be biased towards a particular direction.
 * @param <T> the type of the array objects.
 */
public class PermutationGenerator<T> {

	private final Map<T, Integer> VALUES;
	
	private int valueOf(final T key) {
		return VALUES.get(key).intValue();
	}
	
	private final T[] array;
	private boolean firstReady;

	public PermutationGenerator(final T[] all) {
		VALUES = new HashMap<>();
		for (int i = 0; i < all.length; i++) {
			VALUES.put(all[i], Integer.valueOf(i));
		}
		array = all.clone();
	}
	
	public List<T[]> all() {
		int size = array.length;
		for (int i = 2; i < array.length; i++) {
			size *= i;
		}
		final List<T[]> result = new ArrayList<>(size);
		while (hasMore()) {
			result.add(getNext());
		}
		return result;
	}

	public boolean hasMore() {
		boolean end = firstReady;
		for (int i = 1; i < array.length; i++) {
			end = end && valueOf(array[i]) < valueOf(array[i - 1]);
		}
		return !end;
	}

	public T[] getNext() {
		if (!firstReady) {
			firstReady = true;
			return array.clone();
		}
		T temp;
		int j = array.length - 2;
		int k = array.length - 1;

		// Find largest index j with a[j] < a[j+1]
		for (; valueOf(array[j]) > valueOf(array[j + 1]); j--);

		// Find index k such that a[k] is smallest integer
		// greater than a[j] to the right of a[j]
		for (; valueOf(array[j]) > valueOf(array[k]); k--);

		// Interchange a[j] and a[k]
		temp = array[k];
		array[k] = array[j];
		array[j] = temp;

		// Put tail end of permutation after jth position in increasing order
		int r = array.length - 1;
		int s = j + 1;

		while (r > s) {
			temp = array[s];
			array[s++] = array[r];
			array[r--] = temp;
		}

		return array.clone();
	}

}