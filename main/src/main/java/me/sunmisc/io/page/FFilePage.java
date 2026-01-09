package me.sunmisc.io.page;

import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;

import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.nio.file.StandardOpenOption.*;

public final class FFilePage implements Page {
    private final MemorySegment segment;
    private final long offset;

    public FFilePage(final MemorySegment segment, final long offset) {
        this.segment = segment;
        this.offset = offset;
    }

    public FFilePage(final File origin, final long offset, final int size) throws IOException {
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
        return segment.getAtIndex(JAVA_LONG, index);
    }

    @Override
    public void put(final int index, final long value) {
        segment.setAtIndex(JAVA_LONG, index, value);
    }

    @Override
    public long compareAndExchange(final int index, final long expected, final long value) {
        return (long) JAVA_LONG
                .varHandle()
                .compareAndExchange(
                        segment,
                        index * JAVA_LONG.byteSize(),
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
}

