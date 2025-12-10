package sunmisc.io;

import me.sunmisc.io.Table;
import me.sunmisc.io.UnsignedIntHashTable;
import me.sunmisc.io.alloc.*;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UnsignedIntHashTableTest {
    private static final String FILE_NAME = "test.bin";
    private static final int MAX_SIZE = 1_000_000;
    private Alloc allocation;

    @BeforeTest
    public void init() {
        final File file = new File(FILE_NAME);
        file.delete();
        this.allocation = new AllocAlignedPage(new AllocIntPage(new AllocFileDefault(file)));
    }

    @AfterTest
    public void before() {
        new File(FILE_NAME).delete();
    }

    @Test
    public void testPutSeq() throws IOException {
        final Map<Integer, Integer> map = new HashMap<>(MAX_SIZE);
        final Table table = new UnsignedIntHashTable(this.allocation);
        for (int i = 0; i < MAX_SIZE; ++i) {
            map.put(i, -i);
            table.put(i, -i);
        }
        for (int i = 0; i < MAX_SIZE; ++i) {
            Assert.assertEquals(table.get(i).orElse(null), map.get(i));
        }
    }

    @Test
    public void testPutRand() throws IOException {
        final Map<Integer, Integer> map = new HashMap<>(MAX_SIZE);
        final Table table = new UnsignedIntHashTable(this.allocation);
        final Random random = new Random(122);
        for (int i = 0; i < MAX_SIZE; ++i) {
            final int v = random.nextInt(0, MAX_SIZE);
            map.put(v, -v);
            table.put(v, -v);
        }
        for (final int k : map.keySet()) {
            Assert.assertEquals(table.get(k).orElse(null), map.get(k));
        }
    }
}
