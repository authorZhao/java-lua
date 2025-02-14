// Generated by jextract

package com.git.lua;

import java.lang.invoke.*;
import java.lang.foreign.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;

/**
 * {@snippet lang=c :
 * typedef int (*lua_Writer)(lua_State *, const void *, size_t, void *)
 * }
 */
public class lua_Writer {

    lua_Writer() {
        // Should not be called directly
    }

    /**
     * The function pointer signature, expressed as a functional interface
     */
    public interface Function {
        int apply(MemorySegment L, MemorySegment p, long sz, MemorySegment ud);
    }

    private static final FunctionDescriptor $DESC = FunctionDescriptor.of(
        luahpp_h.C_INT,
        luahpp_h.C_POINTER,
        luahpp_h.C_POINTER,
        luahpp_h.C_LONG_LONG,
        luahpp_h.C_POINTER
    );

    /**
     * The descriptor of this function pointer
     */
    public static FunctionDescriptor descriptor() {
        return $DESC;
    }

    private static final MethodHandle UP$MH = luahpp_h.upcallHandle(lua_Writer.Function.class, "apply", $DESC);

    /**
     * Allocates a new upcall stub, whose implementation is defined by {@code fi}.
     * The lifetime of the returned segment is managed by {@code arena}
     */
    public static MemorySegment allocate(lua_Writer.Function fi, Arena arena) {
        return Linker.nativeLinker().upcallStub(UP$MH.bindTo(fi), $DESC, arena);
    }

    private static final MethodHandle DOWN$MH = Linker.nativeLinker().downcallHandle($DESC);

    /**
     * Invoke the upcall stub {@code funcPtr}, with given parameters
     */
    public static int invoke(MemorySegment funcPtr,MemorySegment L, MemorySegment p, long sz, MemorySegment ud) {
        try {
            return (int) DOWN$MH.invokeExact(funcPtr, L, p, sz, ud);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}

