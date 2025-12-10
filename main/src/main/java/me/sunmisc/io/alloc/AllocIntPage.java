package me.sunmisc.io.alloc;

import me.sunmisc.io.IntAlignedPage;
import me.sunmisc.io.Location;
import me.sunmisc.io.page.Page;

import java.io.IOException;

public final class AllocIntPage implements Alloc {
    private final Alloc origin;

    public AllocIntPage(final Alloc origin) {
        this.origin = origin;
    }

    @Override
    public Page alloc(final int size) throws IOException {
        final Page page = new IntAlignedPage(
                this.origin.alloc(size << 2)
        );
        // fill page
        for (int i = 0; i < size; i += 2) {
            page.writeInt(i, -1);
        }
        return page;
    }

    @Override
    public Page take(final Location location) throws IOException {
        return this.origin.take(location);
    }

    @Override
    public void free(final Location location) throws IOException {

    }
}
