package me.sunmisc.io.page;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class BufferPage implements Page {
    private final long offset;
    private final ByteBuffer buffer;

    public BufferPage(final long offset, final ByteBuffer buffer) {
        this.offset = offset;
        this.buffer = buffer;
    }

    @Override
    public void writeInt(final int index, final int value) throws IOException {
        this.buffer.putInt(index, value);
    }

    @Override
    public int readInt(final int index) throws IOException {
        return this.buffer.getInt(index);
    }

    @Override
    public void writeLong(final int index, final long value) throws IOException {
        this.buffer.putLong(index, value);
    }

    @Override
    public long readLong(final int index) throws IOException {
        return this.buffer.getLong(index);
    }

    @Override
    public int length() {
        return this.buffer.capacity();
    }

    @Override
    public long offset() {
        return this.offset;
    }
}
