package me.sunmisc.io;

import me.sunmisc.calculus.Cursor;

import java.io.IOException;

public interface Vector extends Iterable<Integer> {

    void add(int value) throws IOException;

    int get(int pos) throws IOException;

    void remove(int pos) throws IOException;

    int size() throws IOException;
    
    Cursor<Integer> cursor() throws IOException;
}
