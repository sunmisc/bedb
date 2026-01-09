package sunmisc.io;

import org.testng.annotations.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.testng.Assert.assertEquals;

public class FfApi {

    @Test
    public void testCas() {
        Arena arena = Arena.ofAuto();
        MemorySegment segment = arena.allocate(8);
        VarHandle arrayHandle = ValueLayout.JAVA_INT.varHandle();

        VarHandle mh = MethodHandles.insertCoordinates(arrayHandle, 0, segment, 0);
        mh.compareAndSet(0, 55);
        assertEquals(mh.get(), 55);
    }
}
