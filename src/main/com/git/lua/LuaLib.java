package com.git.lua;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * @author authorZhao
 * @since 2024-03-21
 */
public class LuaLib {
    private static final Arena LIBRARY_ARENA = Arena.ofAuto();
    private static Path path;
    private static final String libName = "liblua5.4.0";
    static {
        try {
            String property = System.getProperty("os.name", "").toLowerCase();
            if (property.contains("win")) {
                path = Path.of(LuaLib.class.getResource(libName + ".dll").toURI());
            } else {
                path = Path.of(LuaLib.class.getResource(libName + ".so").toURI());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static final SymbolLookup SYMBOL_LOOKUP = SymbolLookup.libraryLookup(path, LIBRARY_ARENA)
            .or(SymbolLookup.loaderLookup())
            .or(Linker.nativeLinker().defaultLookup());
}
