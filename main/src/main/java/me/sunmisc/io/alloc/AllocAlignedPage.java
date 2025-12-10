package me.sunmisc.io.alloc;

import me.sunmisc.io.Location;
import me.sunmisc.io.page.Page;

import java.io.IOException;

import static java.lang.Integer.numberOfLeadingZeros;

public final class AllocAlignedPage implements Alloc {
    private final Alloc origin;

    public AllocAlignedPage(final Alloc origin) {
        this.origin = origin;
    }

    @Override
    public Page alloc(final int size) throws IOException {
        final int threshold = (-1 >>> numberOfLeadingZeros(size - 1)) + 1;
        return this.origin.alloc(threshold);
    }

    @Override
    public Page take(final Location location) throws IOException {
        return this.origin.take(location);
    }

    @Override
    public void free(final Location location) throws IOException {

    }
}
