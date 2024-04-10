package com.git.script;

import com.git.test.TestLua3;
import com.git.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.git.lua.lauxlib_h.luaL_loadstring;

/**
 * @author authorZhao
 * @since 2024-04-09
 */
public class LuaScriptUtil {
    private LuaScriptUtil() {
        throw new AssertionError();
    }

    /**
     * 根据相对录像读取文件内容
     * 
     * @param filename
     * @return
     */
    public static String readLuaScript(String filename) throws Exception {
        URL resource = LuaScriptUtil.class.getResource(filename);
        try (InputStream inputStream = resource.openStream()) {
            String luaCode = FileUtils.readString(inputStream);
            if (luaCode == null) {
                System.err.println("luaCode is null");
            }
            return luaCode;
        }
    }
}
