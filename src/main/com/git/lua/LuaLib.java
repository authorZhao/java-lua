package com.git.lua;

import com.git.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author authorZhao
 * @since 2024-03-21
 */
public class LuaLib {
    private static final Arena LIBRARY_ARENA = Arena.ofAuto();
    private static Path path;
    private static final String LIB_NAME = "lua5.4.4";

    static {
        URL location = LuaLib.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println("jarPath = " + location);
        if (isJarEnv()) {
            acquireLuaPathInJar();
        } else {
            acquireLuaPath();
        }
    }

    private static void acquireLuaPath() {
        try {
            String property = System.getProperty("os.name", "").toLowerCase();
            if (property.contains("win")) {
                path = Path.of(LuaLib.class.getResource(LIB_NAME + ".dll").toURI());
            } else {
                path = Path.of(LuaLib.class.getResource(LIB_NAME + ".so").toURI());
            }
            System.out.println("path = " + path);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void acquireLuaPathInJar() {
        try {
            String property = System.getProperty("os.name", "").toLowerCase();
            URL url3 = LuaLib.class.getResource(LIB_NAME + ".dll");
            if (property.contains("win")) {
                path = Files.createTempFile(LIB_NAME, ".dll");
                FileUtils.copy(url3.openStream(), path);
            } else {
                path = Files.createTempFile(LIB_NAME, ".so");
            }
            deleteTempFile(path, property);
            System.out.println("path = " + path);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void deleteTempFile(Path path, String osName) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if(!LIBRARY_ARENA.scope().isAlive()){
                    Files.delete(path);
                    System.out.println("delete lualib absolutePath = " + path.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
                //throw new RuntimeException(e);
            }
        }));
        if (osName.contains("linux")) {
            Files.delete(path);
            return;
        }
        File file = path.toFile();
        File[] files = file.getParentFile().listFiles();
        if(files==null){
            return;
        }
        for (File file1 : files) {
            if (file1.isFile() && file1.getName().startsWith(LIB_NAME) && !file1.getName().equals(file.getName())) {
                String absolutePath = file1.getAbsolutePath();
                file1.delete();
                System.out.println("delete last lualib absolutePath = " + absolutePath);
            }
        }
    }


    public static final SymbolLookup SYMBOL_LOOKUP = SymbolLookup.libraryLookup(path, LIBRARY_ARENA)
            .or(SymbolLookup.loaderLookup())
            .or(Linker.nativeLinker().defaultLookup());

    private static boolean isJarEnv() {
        String filePath = LuaLib.class.getResource(LuaLib.class.getSimpleName() + ".class").toString();
        return filePath.startsWith("jar:file");
    }

}
