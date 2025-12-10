package me.sunmisc.io.alloc;

import me.sunmisc.io.Location;
import me.sunmisc.io.page.Page;

import java.io.IOException;

public interface Alloc {

    Page alloc(int size) throws IOException;

    Page take(Location location) throws IOException;

    void free(Location location) throws IOException;
}
