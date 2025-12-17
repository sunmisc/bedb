package me.sunmisc.io.alloc;

import me.sunmisc.io.Location;
import me.sunmisc.io.page.HeapPage;
import me.sunmisc.io.page.Page;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class AllocHeapTable implements Alloc {
    private final AtomicInteger ids = new AtomicInteger();
    private final ConcurrentMap<Long, Page> pages = new ConcurrentHashMap<>();

    @Override
    public Page alloc(final int size) {
        //          header
        // +------------------------+
        // | size                   |
        // | segments [0..30]       |
        // +------------------------+
        //                  segments
        // +---------------------------------------+
        // | [16] [.32] [..64] [...128] [....256]  |
        // +---------------------------------------+
        final long id = this.ids.getAndAdd(size);
        final Page page = new HeapPage(id, size);
        if (this.pages.putIfAbsent(id, page) != null) {
            throw new IllegalStateException();
        }
        return page;
    }

    @Override
    public Page take(final Location offset) {
        return Objects.requireNonNull(this.pages.get(offset.offset()));
    }
    @Override
    public void free(final Location location) throws IOException {
        this.pages.remove(location.offset());
    }
}
