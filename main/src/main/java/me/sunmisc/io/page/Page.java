package me.sunmisc.io.page;

import me.sunmisc.io.Location;
import java.io.IOException;

public interface Page extends Location {
    long LOW_MASK  = 0xFFFFFFFFL;
    long HIGH_MASK = 0xFFFFFFFF00000000L;

    @Deprecated
    default void writeInt(int index, int value) throws IOException {
        final int longIndex = index >>> 1;
        final boolean high = (index & 1) == 0;
        final long mask = high ? LOW_MASK : HIGH_MASK;
        final long shifted = high
                ? ((long) value << 32)
                : (value & LOW_MASK);
        for (;;) {
            final long val = take(longIndex);
            final long next = (val & mask) | shifted;
            if (compareAndExchange(longIndex, val, next) == val) {
                return;
            }
        }
    }

    @Deprecated
    default int readInt(int index) throws IOException {
        final int longIndex = index >>> 1;
        final boolean high = (index & 1) == 0;
        return high ? (int) (take(longIndex) >>> 32) : (int) take(longIndex);
    }

    @Deprecated
    void writeLong(int index, long value) throws IOException;

    @Deprecated
    long readLong(int index) throws IOException;

    int length();

    // vse long
    // кому надо сжатие - пускай откусывают от лонга (ака bit set)
    // new api
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
        return 0L;
    }

}
