package me.sunmisc.io.page;

import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import static java.lang.foreign.ValueLayout.*;
import static java.nio.file.StandardOpenOption.*;

public final class AtMemoryPage implements AtomicPage {
    private final MemorySegment segment;
    private final long offset;

    public AtMemoryPage(final MemorySegment segment, final long offset) {
        this.segment = segment;
        this.offset = offset;
    }

    public AtMemoryPage(final File origin, final long offset, final int size) throws IOException {
        MemorySegment segment;
        try (final FileChannel channel = FileChannel.open(origin.toPath(), CREATE, READ, WRITE)) {
            segment = channel.map(
                    FileChannel.MapMode.READ_WRITE,
                    offset, size,
                    Arena.ofAuto()
            );
        }
        this(segment, offset);
    }

    @Deprecated
    public void writeInt(int index, int value) throws IOException {
        JAVA_INT.varHandle().setVolatile(segment, index, value);
    }

    @Override
    public boolean weakCompareAndSet(int index, long expected, long value) {
        return JAVA_LONG
                .varHandle()
                .weakCompareAndSet(segment, index, expected, value);
    }

    @Deprecated
    public int readInt(int index) throws IOException {
        return (int) JAVA_INT.varHandle().getVolatile(segment, index);
    }

    @Override
    public void writeLong(final int index, final long value) throws IOException {
        put(index, value);
    }

    @Override
    public long readLong(final int index) throws IOException {
        return take(index);
    }

    @Override
    public long take(final int index) {
        return (long) JAVA_LONG.varHandle().getVolatile(segment, index);
    }

    @Override
    public void put(final int index, final long value) {
        JAVA_LONG.varHandle().setVolatile(segment, index, value);
    }

    @Override
    public long compareAndExchange(final int index, final long expected, final long value) {
        return (long) JAVA_LONG
                .varHandle()
                .compareAndExchange(
                        segment,
                        index,
                        expected,
                        value
                );
    }

    @Override
    public int length() {
        return Math.toIntExact(segment.byteSize());
    }

    @Override
    public long offset() {
        return offset;
    }

    @Override
    public String toString() {
        return Arrays.toString(segment.toArray(JAVA_BYTE));
    }
}
