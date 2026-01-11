package me.sunmisc.io.page;

import me.sunmisc.io.Location;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.ByteOrder;

import static java.lang.foreign.ValueLayout.JAVA_LONG;

public interface Page extends Location {
    long LOW_MASK  = 0xFFFFFFFFL;

    public static void main(String[] args) throws IOException {
        Page page = new FFilePage(Arena.ofAuto().allocate(1024 * 4), 0);
        page.writeInt(0, 1222);
        System.out.println(page.take(0) >>> 32);
        System.out.println(page.readLong(1));
        System.out.println(page.readInt(2));
        System.out.println(page.readLong(3));
    }

    @Deprecated
    default void writeInt(int index, int value) throws IOException {
        if ((index & 3) != 0) {
            throw new IllegalArgumentException("Index must be 4-byte aligned");
        }
        final int wordOffset = index & ~7;
        final boolean high = (index & 4) == 0;
        final int shift = high ? 0 : 32;
        final long mask = LOW_MASK << shift;
        final long maskedX = (value & LOW_MASK) << shift;

        final long fullWord = take(wordOffset);
        put(wordOffset, (fullWord & ~mask) | maskedX);
    }

    @Deprecated
    default int readInt(int index) throws IOException {
        final int wordOffset = index & ~7;
        final boolean high = (index & 4) == 0;
        final long v = take(wordOffset);
        return (int) (high ? (v >>> 32) : v);
    }

    @Deprecated
    void writeLong(int index, long value) throws IOException;

    @Deprecated
    long readLong(int index) throws IOException;

    int length();

    default long take(int index) {
        try {
            return readLong(index);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void put(int index, long value) {
        try {
            writeLong(index, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default long compareAndExchange(int index, long expected, long value) {
        throw new UnsupportedOperationException();
    }
}
