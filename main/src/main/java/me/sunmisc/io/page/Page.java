package me.sunmisc.io.page;

import me.sunmisc.io.Location;

import java.io.IOException;

public interface Page extends Location {

    @Deprecated
    void writeInt(int index, int value) throws IOException;

    @Deprecated
    int readInt(int index) throws IOException;

    void writeLong(int index, long value) throws IOException;

    long readLong(int index) throws IOException;

    int length();

    // range
}
