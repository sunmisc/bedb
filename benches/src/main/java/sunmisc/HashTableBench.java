package sunmisc;

import me.sunmisc.io.UnsignedIntHashTable;
import me.sunmisc.io.alloc.*;
import me.sunmisc.io.Table;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(1)
@Threads(1)
@BenchmarkMode({Mode.Throughput})
@State(Scope.Thread)
public class HashTableBench {
    private static final long SEED = 3423;
    private static final int DEFAULT_CAPACITY = 128;
    private static final int SIZE = 100_000;

    private Map<Integer, Integer> jdkHashMap;
    private List<Integer> keys;

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(HashTableBench.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void prepare() {
        this.keys = new Random(SEED).ints(SIZE, 0, SIZE).boxed().toList();
        this.jdkHashMap = this.keys
                .stream()
                .collect(
                        IdentityHashMap::new,
                        (map, i) -> map.put(i, i),
                        Map::putAll
                );
    }

    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 2, time = 1)
    @Measurement(iterations = 8, time = 1)
    @Fork(1)
    @Threads(1)
    @BenchmarkMode({org.openjdk.jmh.annotations.Mode.Throughput})
    @State(Scope.Thread)
    public static class Chained {
        private static final String FILE_NAME = "bench.bin";
        private Table flatHashTable;
        private List<Integer> keys;
        private @Param Mode mode;

        @Setup
        public void init() throws IOException {
            final Alloc alloc = switch (this.mode) {
                case FILE -> {
                    final File file = new File(FILE_NAME);
                    file.delete();
                    yield new AllocAlignedPage(new AllocIntPage(new AllocFileDefault(file)));
                }
                case HEAP -> new AllocAlignedPage(new AllocHeapTable());
            };
            this.keys = new Random(SEED).ints(SIZE, 0, SIZE).boxed().toList();
            this.flatHashTable = new UnsignedIntHashTable(alloc, DEFAULT_CAPACITY);
            for (final int k : this.keys) {
                this.flatHashTable.put(k, -k);
            }
        }

        @TearDown
        public void before() {
            new File(FILE_NAME).delete();
        }

        @Benchmark
        public Optional<Integer> hashTableGet() throws IOException {
            final int key = ThreadLocalRandom.current().nextInt(0, SIZE);
            return this.flatHashTable.get(key);
        }

        @Benchmark
        public int hashTablePut() throws IOException {
            final int idx = ThreadLocalRandom.current().nextInt(0, SIZE);
            final int key = this.keys.get(idx);
            this.flatHashTable.put(key, idx);
            return key;
        }

        public enum Mode { FILE, HEAP }
    }

    @Benchmark
    public int jdkHashMapPut() {
        final int idx = ThreadLocalRandom.current().nextInt(0, SIZE);
        final int key = this.keys.get(idx);
        this.jdkHashMap.put(key, idx);
        return key;
    }

    @Benchmark
    public Optional<Integer> jdkHashMapGet() {
        final int key = ThreadLocalRandom.current().nextInt(0, SIZE);
        return Optional.ofNullable(this.jdkHashMap.get(key));
    }
}
