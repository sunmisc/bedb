package me.sunmisc.io.alloc;

import me.sunmisc.io.Location;
import me.sunmisc.io.page.Page;

import java.io.IOException;

public final class AllocFileTable implements Alloc {
    private final Alloc alloc;

    public AllocFileTable(Alloc alloc) {
        this.alloc = alloc;
    }

    @Override
    public Page alloc(int size) throws IOException {
        //          header
        // +------------------------+
        // | size                   |
        // | segments [0..30]       |
        // +------------------------+
        //                  segments
        // +---------------------------------------+
        // | [16] [.32] [..64] [...128] [....256]  |
        // +---------------------------------------+
        return null;
    }

    @Override
    public Page take(Location location) throws IOException {
        return null;
    }

    @Override
    public void free(Location location) throws IOException {

    }
}
