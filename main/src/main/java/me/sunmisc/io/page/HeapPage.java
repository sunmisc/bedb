package me.sunmisc.io.page;

import java.util.Arrays;

public final class HeapPage implements Page {
    private final int[] elements;
    private final long offset;
    private final int size;

    public HeapPage(final long offset, final int size) {
        this.offset = offset;
        this.size = size;
        this.elements = new int[size];
        Arrays.fill(this.elements, -1);
    }

    @Override
    public void writeInt(final int index, final int value) {
        this.elements[index] = value;
    }

    @Override
    public int readInt(final int index) {
        return this.elements[index];
    }

    @Override
    public void writeLong(final int index, final long value) {
        this.elements[index] = (int)(value >>> 32);
        this.elements[index + 1] = (int)(value);
    }

    @Override
    public long readLong(final int index) {
        final long high = ((long) this.elements[index]) & 0xFFFFFFFFL;
        final long low  = ((long) this.elements[index + 1]) & 0xFFFFFFFFL;
        return (high << 32) | low;
    }

    @Override
    public int length() {
        return this.size;
    }

    @Override
    public long offset() {
        return this.offset;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.elements);
    }
}
