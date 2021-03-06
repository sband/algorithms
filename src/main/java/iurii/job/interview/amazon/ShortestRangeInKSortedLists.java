package iurii.job.interview.amazon;

import java.util.*;

/**
 * Problem statement in detail:
 * Given k sorted lists of integers of size n each,
 * find the smallest range that includes at least element from each of the k lists.
 * If more than one smallest ranges are found, print any one of them.
 * <p>
 * Problem is similar to {@link iurii.job.interview.facebook.MinIteratorOfSortedAscendingLists}
 * <p>
 * https://www.youtube.com/watch?v=zplklOy7ENo
 */
public class ShortestRangeInKSortedLists {

    /**
     * Basic solution without priorityQueue and any constructs.
     * Each step increase the pointer in list which has smallest current value
     */
    public Range findRange(List<List<Integer>> lists) {
        // checks if needed
        if (lists == null || lists.isEmpty()) {
            return null;
        }
        if (lists.stream().anyMatch(List::isEmpty)) {
            return null;
        }
        long totalTransitionSize = lists.stream().mapToLong(list -> list.size() - 1).sum();
        // pointers
        int listsCount = lists.size();
        List<Integer> pointers = new ArrayList<>(Collections.nCopies(listsCount, 0));
        // initial value - will be changed, because min > max
        Range result = new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);
        // find
        do { // check all transitions
            long min = Integer.MAX_VALUE;
            long max = Integer.MIN_VALUE;
            int minFromExisting = Integer.MAX_VALUE;
            int minPointer = -1;
            for (int i = 0; i < listsCount; i++) {
                int pointer = pointers.get(i);
                max = Math.max(lists.get(i).get(pointer), max);
                min = Math.min(lists.get(i).get(pointer), min);
                // find min from lists that have one more element
                if (lists.get(i).get(pointer) < minFromExisting && pointer + 1 < lists.get(i).size()) {
                    minFromExisting = Math.min(lists.get(i).get(pointer), minFromExisting);
                    minPointer = i;
                }
            }
            if (minPointer != -1) {
                pointers.set(minPointer, pointers.get(minPointer) + 1);
            }
            if (max - min < result.max - result.min) {
                result.min = min;
                result.max = max;
            }
        } while (totalTransitionSize-- > 0);
        return result;
    }

    // long not to have overflow in case wide range of integers
    public static class Range {
        private long min;
        private long max;

        Range(long min, long max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return String.format("[%d]-[%d]", min, max);
        }

        public long getMin() {
            return min;
        }

        public long getMax() {
            return max;
        }
    }

    public Range findRangeWithPriorityQueue(List<List<Integer>> lists) {
        // checks if needed
        if (lists == null || lists.isEmpty()) {
            return null;
        }
        if (lists.stream().anyMatch(List::isEmpty)) {
            return null;
        }
        Range result = new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);
        MixedListsIterator iterator = new MixedListsIterator(lists);
        do {
            Range range = iterator.next();
            if (range.max - range.min < result.max - result.min) {
                result = range;
            }
        } while (iterator.hasNext());
        return result;
    }

    interface MinIteratorOfSortedAscendingLists<E> extends Iterator<E> {
    }

    private class MixedListsIterator implements MinIteratorOfSortedAscendingLists<Range> {

        private final PriorityQueue<ListIterator> minQueue;
        private final PriorityQueue<Integer> maxQueue;
        private long totalTransitionSize;

        MixedListsIterator(List<List<Integer>> lists) {
            minQueue = new PriorityQueue<>(lists.size());
            maxQueue = new PriorityQueue<>(lists.size(), Comparator.reverseOrder());
            for (List<Integer> list : lists) {
                minQueue.add(new ListIterator(list.iterator()));
                maxQueue.add(list.get(0));
            }
            totalTransitionSize = lists.stream().mapToLong(list -> list.size() - 1).sum() + 1;
        }

        @Override
        public boolean hasNext() {
            return totalTransitionSize != 0;
        }

        @Override
        public Range next() {
            if (hasNext()) {
                ListIterator listIterator = minQueue.poll();
                Range result = new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);
                if (listIterator.hasNext()) {
                    int min = listIterator.next();
                    int max = maxQueue.peek();
                    result.min = min;
                    result.max = max;
                    maxQueue.remove(min);
                    minQueue.add(listIterator);
                    if (listIterator.hasNext()) {
                        maxQueue.add(listIterator.value);
                    }
                }
                totalTransitionSize--;
                return result;
            }
            throw new NoSuchElementException();
        }
    }

    public static class ListIterator implements Comparable<ListIterator>, Iterator<Integer> {
        private Iterator<Integer> iterator;
        private Integer value;

        ListIterator(Iterator<Integer> iterator) {
            this.iterator = iterator;
            if (iterator.hasNext()) {
                value = iterator.next();
            }
        }

        @Override
        public int compareTo(ListIterator o) {
            if (hasNext() && o.hasNext()) {
                return Integer.compare(value, o.value);
            } else {
                if (!hasNext() && !o.hasNext()) {
                    return 0;
                } else {
                    return hasNext() ? -1 : 1;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return value != null;
        }

        @Override
        public Integer next() {
            int result = value;
            if (iterator.hasNext()) {
                value = iterator.next();
            } else {
                value = null;
            }
            return result;
        }
    }
}
