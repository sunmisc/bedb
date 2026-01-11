package me.sunmisc.io.page;

import java.io.IOException;

public interface AtomicPage extends Page {

    @Override
    default void writeInt(int index, int value) throws IOException {
        if ((index & 3) != 0) {
            throw new IllegalArgumentException("Index must be 4-byte aligned");
        }
        final int wordOffset = index & ~7;
        final boolean high = (index & 4) == 0;
        final int shift = high ? 0 : 32;
        final long mask = LOW_MASK << shift;
        final long maskedX = (value & LOW_MASK) << shift;
        long fullWord;
        do {
            fullWord = take(wordOffset);
        } while (!weakCompareAndSet(wordOffset, fullWord, (fullWord & ~mask) | maskedX));
    }

    @Override
    default int readInt(int index) throws IOException {
        final int wordOffset = index & ~7;
        final boolean high = (index & 4) == 0;
        long v = take(wordOffset);
        return (int) (high ? (v >>> 32) : v);
    }

    long compareAndExchange(int index, long expected, long value);

    default boolean compareAndSet(int index, long expected, long value) {
        return compareAndExchange(index, expected, value) == expected;
    }

    default boolean weakCompareAndSet(int index, long expected, long value) {
        return compareAndSet(index, expected, value);
    }

    default long getAndAddInt(int index, long delta) {
        long current;
        do {
            current = take(index);
        } while (!weakCompareAndSet(index, current, current + delta));
        return current;
    }

    default long getAndBitwiseOrLong(int index, long mask) {
        long current;
        do {
            current = take(index);
        } while (!weakCompareAndSet(index, current, current | mask));
        return current;
    }

    default long getAndBitwiseAndLong(int index, long mask) {
        long current;
        do {
            current = take(index);
        } while (!weakCompareAndSet(index, current, current & mask));
        return current;
    }

    default long getAndBitwiseXorLong(int index, long mask) {
        long current;
        do {
            current = take(index);
        } while (!weakCompareAndSet(index, current, current ^ mask));
        return current;
    }
}
