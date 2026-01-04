package me.sunmisc.io;

import me.sunmisc.io.alloc.Alloc;
import me.sunmisc.io.alloc.AllocHeapTable;
import me.sunmisc.io.alloc.AllocIntPage;
import me.sunmisc.io.page.Page;
import me.sunmisc.io.page.SegmentsPage;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class HeaderList extends AbstractList<Page> {
    private final Alloc alloc;
    private final Page header;
    private final Lock lock = new ReentrantLock();

    public HeaderList(final Alloc alloc, final Page header) {
        this.alloc = alloc;
        this.header = header;
    }

    @Override
    public Page get(final int index) {
        Objects.checkIndex(index, size());
        try {
            final long offset = this.header.readLong((index << 1) + 1);
            return this.alloc.take(new Location.LongLocation(offset));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean add(final Page page) {
        lock.lock();
        try {
            final int pos = Math.max(header.readInt(0), 0);
            header.writeLong((pos << 1) + 1, page.offset());
            header.writeInt(0, pos + 1);
            return true;
        } catch (final IOException ex) {
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            return header.readInt(0);
        } catch (IOException e) {
            return 0;
        }
    }
}
