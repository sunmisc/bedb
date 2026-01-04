package me.sunmisc.io;

import java.io.IOException;
import java.util.Optional;

public interface Free {

    Optional<Location> poll(int require) throws IOException;

    void add(Iterable<Location> locations) throws IOException;

    int size();
}
