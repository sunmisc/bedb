package me.sunmisc.io;

import java.io.IOException;
import java.util.Optional;

public interface Lifo {

    Optional<Location> poll() throws IOException;

    void add(Iterable<Location> locations) throws IOException;

    default void clear() throws IOException {
        for (Optional<Location> opt = this.poll(); opt.isPresent(); opt = this.poll());
    }

    int size();
}
