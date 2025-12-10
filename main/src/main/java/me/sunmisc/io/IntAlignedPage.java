package me.sunmisc.io;

import me.sunmisc.io.page.Page;

import java.io.IOException;

public final class IntAlignedPage implements Page {
    private final Page origin;

    public IntAlignedPage(final Page origin) {
        this.origin = origin;
    }

    @Override
    public void writeInt(final int index, final int value) throws IOException {
        this.origin.writeInt(index << 2, value);
    }

    @Override
    public int readInt(final int index) throws IOException {
        return this.origin.readInt(index << 2);
    }

    @Override
    public void writeLong(final int index, final long value) throws IOException {
        this.origin.writeLong(index << 2, value);
    }

    @Override
    public long readLong(final int index) throws IOException {
        return this.origin.readLong(index << 2);
    }

    @Override
    public int length() {
        return this.origin.length() >>> 2;
    }

    @Override
    public long offset() {
        return this.origin.offset();
    }
}
