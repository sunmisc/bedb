package me.sunmisc.io.page;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public final class FilePage implements Page, AutoCloseable {
    private final RandomAccessFile raf;
    private final ByteBuffer buffer;
    private final long offset;
    private final int size;

    public FilePage(final File origin, final long offset, final int size) throws IOException {
        this.raf = new RandomAccessFile(origin, "rw");
        this.offset = offset;
        this.size = size;
        this.buffer = this.raf.getChannel().map(
                FileChannel.MapMode.READ_WRITE,
                offset,
                size
        ).order(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public void writeInt(final int index, final int value) {
        this.buffer.putInt(index, value);
    }

    @Override
    public int readInt(final int index) {
        return this.buffer.getInt(index);
    }

    @Override
    public void writeLong(final int index, final long value) {
        this.buffer.putLong(index, value);
    }

    @Override
    public long readLong(final int index) {
        return this.buffer.getLong(index);
    }

    @Override
    public long offset() {
        return this.offset;
    }

    @Override
    public int length() {
        return this.size;
    }

    @Override
    public void close() throws IOException {
        this.raf.close();
    }

    @Override
    public String toString() {
        return Arrays.toString(this.buffer.array());
    }
}
