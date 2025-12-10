package me.sunmisc.io;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Table extends Location, Iterable<Map.Entry<Integer, Integer>> {

    void put(int key, int value) throws IOException;

    Optional<Integer> get(int key) throws IOException;

    int size();

    default boolean containsKey(final int key) throws IOException {
        return this.get(key).isPresent();
    }

    default IntStream keys() {
        return this.stream().mapToInt(Map.Entry::getKey);
    }

    default IntStream values() {
        return this.stream().mapToInt(Map.Entry::getValue);
    }

    default Stream<Map.Entry<Integer, Integer>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}
