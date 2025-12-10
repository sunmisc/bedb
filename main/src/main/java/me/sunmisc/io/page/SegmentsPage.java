package me.sunmisc.io.page;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.Integer.numberOfLeadingZeros;

public final class SegmentsPage implements Page {
    private final List<Page> pages;

    public SegmentsPage(final List<Page> pages) {
        this.pages = pages;
    }

    private static int log_2(final int x) {
        return 31 - numberOfLeadingZeros(x);
    }

    private int segmentSize(final int index) {
        final int shift = this.pages.getFirst().length();
        return (shift << (index + 1)) - shift;
    }

    private Page pageForIndex(final int index) {
        final int shift = this.pages.getFirst().length();
        // log_2((index + shift) / shift)
        final int exp = log_2(index + shift) - log_2(shift);
        return this.pages.get(exp);
    }

    private int indexForSegment(final Page segment, final int index) {
        final int shift = this.pages.getFirst().length();
        return index - segment.length() + shift;
    }

    @Override
    public void writeInt(final int index, final int value) throws IOException {
        Objects.checkIndex(index, this.length());
        final Page page = this.pageForIndex(index);
        final int idx = this.indexForSegment(page, index);
        page.writeInt(idx, value);
    }

    @Override
    public int readInt(final int index) throws IOException {
        Objects.checkIndex(index, this.length());
        final Page page = this.pageForIndex(index);
        final int idx = this.indexForSegment(page, index);
        return page.readInt(idx);
    }

    @Override
    public void writeLong(final int index, final long value) throws IOException {
        Objects.checkIndex(index, this.length());
        final Page page = this.pageForIndex(index);
        final int idx = this.indexForSegment(page, index);
        page.writeLong(idx, value);
    }

    @Override
    public long readLong(final int index) throws IOException {
        Objects.checkIndex(index, this.length());
        final Page page = this.pageForIndex(index);
        final int idx = this.indexForSegment(page, index);
        return page.readLong(idx);
    }

    @Override
    public int length() {
        return this.segmentSize(this.pages.size() - 1);
    }

    @Override
    public long offset() {
        return this.pages.getFirst().offset();
    }

    @Override
    public String toString() {
        return this.pages.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}
