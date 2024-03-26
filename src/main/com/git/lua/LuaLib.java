package com.git.lua;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;

/**
 * @author authorZhao
 * @since 2024-03-21
 */
public class LuaLib {
    static final Arena LIBRARY_ARENA = Arena.ofAuto();
    public static final SymbolLookup SYMBOL_LOOKUP = SymbolLookup.libraryLookup("E:/tool/lua5.4-zhao/liblua5.4.0.dll", LIBRARY_ARENA)
            .or(SymbolLookup.loaderLookup())
            .or(Linker.nativeLinker().defaultLookup());
}
