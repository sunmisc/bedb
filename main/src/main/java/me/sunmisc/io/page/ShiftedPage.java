package me.sunmisc.io.page;

import java.io.IOException;
import java.util.Objects;

public final class ShiftedPage implements Page {
    private final Page origin;
    private final int offset;
    private final int length;

    public ShiftedPage(final Page origin, final int offset, final int length) {
        this.origin = origin;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public void writeInt(final int index, final int value) throws IOException {
        Objects.checkIndex(index, this.length);
        this.origin.writeInt(index + this.offset, value);
    }

    @Override
    public int readInt(final int index) throws IOException {
        Objects.checkIndex(index, this.length);
        return this.origin.readInt(index + this.offset);
    }

    @Override
    public void writeLong(final int index, final long value) throws IOException {
        Objects.checkIndex(index, this.length);
        this.origin.writeLong(index + this.offset, value);
    }

    @Override
    public long readLong(final int index) throws IOException {
        Objects.checkIndex(index, this.length);
        return this.origin.readLong(index + this.offset);
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public long offset() {
        return this.origin.offset() + this.offset;
    }
}
