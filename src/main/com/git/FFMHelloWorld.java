package com.git;


import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

import static java.lang.foreign.ValueLayout.*;

/**
 * @author authorZhao
 * @since 2024-03-13
 */
public class FFMHelloWorld {
    public static void main(String[] args) throws Throwable {
        testDownCall();
        testUpCall();

    }

    private static void testDownCall() {
        Linker linker = Linker.nativeLinker();
        SymbolLookup defaultLookup = linker.defaultLookup();
        MethodHandle strlenHandle = linker.downcallHandle(
                defaultLookup.find("strlen").orElseThrow(),
                FunctionDescriptor.of(JAVA_LONG, ADDRESS));
        MethodHandle printfHandler = linker.downcallHandle(
                defaultLookup.find("printf").orElseThrow(),
                FunctionDescriptor.of(JAVA_LONG, ADDRESS,JAVA_INT));
        try (Arena offHeap = Arena.ofConfined()) {
            MemorySegment pointers = offHeap.allocateFrom("Hello world%d!");
            System.out.println(strlenHandle.invoke(pointers));  //11
            printfHandler.invoke(pointers,8);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static void testUpCall() throws Throwable {
        Linker linker = Linker.nativeLinker();
        MethodHandle qsort = linker.downcallHandle(
                linker.defaultLookup().find("qsort").get(),
                FunctionDescriptor.ofVoid(ADDRESS, JAVA_LONG, JAVA_LONG, ADDRESS)
        );

        MethodHandle comparHandle
                = MethodHandles.lookup()
                .findStatic(FFMHelloWorld.class, "qsortCompare",
                        MethodType.methodType(int.class,
                                MemorySegment.class,
                                MemorySegment.class));

        MemorySegment comparFunc
                = linker.upcallStub(comparHandle,
                        /* A Java description of a C function
                           implemented by a Java method! */
                FunctionDescriptor.of(JAVA_INT,
                        ADDRESS.withTargetLayout(JAVA_INT),
                        ADDRESS.withTargetLayout(JAVA_INT)),
                Arena.ofAuto());
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment array = allocateIntArray(arena,0, 9, 3, 4, 6, 5, 1, 8, 2, 7);
            qsort.invoke(array, 10L, ValueLayout.JAVA_INT.byteSize(), comparFunc);
            int[] sorted = array.toArray(JAVA_INT);    // [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ]
            System.out.println("sorted = " + Arrays.toString(sorted));
        }
    }

    /**
     * void qsort(void *base, size_t nmemb, size_t size,
     *            int (*compar)(const void *, const void *));
     * @param elem1
     * @param elem2
     * @return
     */
    static int qsortCompare(MemorySegment elem1, MemorySegment elem2) {
        System.out.println("正在执行java回调");
        return Integer.compare(elem1.get(JAVA_INT, 0), elem2.get(JAVA_INT, 0));
    }

    public static MemorySegment allocateIntArray(SegmentAllocator arena, int... arr){
        MemorySegment array = arena.allocate(MemoryLayout.sequenceLayout(10, JAVA_INT));
        for (int j : arr) {
            array.setAtIndex(JAVA_INT, 1L, j);
        }
        return array;
    }
}
