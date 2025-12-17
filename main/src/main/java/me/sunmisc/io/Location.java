package me.sunmisc.io;

public interface Location {

    long offset();

    record LongLocation(long offset) implements Location { }
}
