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
 * typedef const char *(*lua_Reader)(lua_State *, void *, size_t *)
 * }
 */
public class lua_Reader {

    lua_Reader() {
        // Should not be called directly
    }

    /**
     * The function pointer signature, expressed as a functional interface
     */
    public interface Function {
        MemorySegment apply(MemorySegment L, MemorySegment ud, MemorySegment sz);
    }

    private static final FunctionDescriptor $DESC = FunctionDescriptor.of(
        lauxlib_h.C_POINTER,
        lauxlib_h.C_POINTER,
        lauxlib_h.C_POINTER,
        lauxlib_h.C_POINTER
    );

    /**
     * The descriptor of this function pointer
     */
    public static FunctionDescriptor descriptor() {
        return $DESC;
    }

    private static final MethodHandle UP$MH = lauxlib_h.upcallHandle(lua_Reader.Function.class, "apply", $DESC);

    /**
     * Allocates a new upcall stub, whose implementation is defined by {@code fi}.
     * The lifetime of the returned segment is managed by {@code arena}
     */
    public static MemorySegment allocate(lua_Reader.Function fi, Arena arena) {
        return Linker.nativeLinker().upcallStub(UP$MH.bindTo(fi), $DESC, arena);
    }

    private static final MethodHandle DOWN$MH = Linker.nativeLinker().downcallHandle($DESC);

    /**
     * Invoke the upcall stub {@code funcPtr}, with given parameters
     */
    public static MemorySegment invoke(MemorySegment funcPtr,MemorySegment L, MemorySegment ud, MemorySegment sz) {
        try {
            return (MemorySegment) DOWN$MH.invokeExact(funcPtr, L, ud, sz);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}

