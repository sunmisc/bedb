package me.sunmisc.io;

import me.sunmisc.io.alloc.Alloc;
import me.sunmisc.io.page.Page;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class HeaderList extends AbstractList<Page> {
    private final Alloc alloc;
    private final Page header;
    private final AtomicInteger position = new AtomicInteger();

    public HeaderList(final Alloc alloc, final Page header) {
        this.alloc = alloc;
        this.header = header;
    }

    @Override
    public Page get(int index) {
        Objects.checkIndex(index, size());
        try {
            final long offset = this.header.readLong(index);
            return this.alloc.take(new Location.LongLocation(offset));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean add(Page page) {
        final int pos = position.getAndAdd(1);
        try {
            header.writeLong(pos, page.offset());
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }

    @Override
    public int size() {
        return position.get();
    }
}
