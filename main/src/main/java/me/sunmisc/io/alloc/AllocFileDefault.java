package me.sunmisc.io.alloc;

import me.sunmisc.io.Location;
import me.sunmisc.io.page.FilePage;
import me.sunmisc.io.page.Page;
import me.sunmisc.io.page.StripedPagedFile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public final class AllocFileDefault implements Alloc {
    private final AtomicLong ids = new AtomicLong();
    private final File file;

    public AllocFileDefault(final File file) {
        this.file = file;
    }

    @Override
    public Page alloc(final int size) throws IOException {
        final long offset = this.ids.getAndAdd(size + Integer.BYTES);
        try (final FilePage header = new FilePage(this.file, offset, Integer.BYTES)) {
            header.writeInt(0, size);
        }
        return new StripedPagedFile(this.file, offset + Integer.BYTES, size);
    }

    @Override
    public Page take(final Location location) throws IOException {
        final long offset = location.offset() - Integer.BYTES;
        try (final FilePage header = new FilePage(this.file, offset, Integer.BYTES)) {
            final int size = header.readInt(0);
            return new StripedPagedFile(this.file, offset + Integer.BYTES, size);
        }
    }

    @Override
    public void free(final Location location) throws IOException {

    }
}
