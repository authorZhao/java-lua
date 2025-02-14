package com.git.test;

import com.git.po.User;
import com.git.util.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.git.lua.luahpp_h.*;
import static java.lang.foreign.MemorySegment.NULL;

/**
 * @author authorZhao
 */
public class TestLua3 {

    public static void main(String[] args) throws Exception {
//        System.setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles","true");
//        System.setProperty("jextract.trace.downcalls","true");
        String script = "com/git/script/testmath/test2.lua";
        // 允许自定义脚本路径
        if (args != null && args.length > 0) {
            script = args[0];
        }
        // 读取lua字符串
        String luaCode = FileUtils.readFromPath(script);
        if (luaCode == null) {
            System.out.println("ERROR: Lua code not found script=" + script);
            return;
        }
        var lua_State = luaL_newstate();
        if (lua_State == NULL) {
            return;
        }
        // 打开内置库
        luaL_openlibs(lua_State);
        var luaUtil = new LuaUtil();
        // 加载自定义的javaMath库
        LuaMathUtil.openJavaMath(lua_State, luaUtil);
        // 加载自定义的User模块
        UserUtilV2.loadUser(lua_State, User.class, luaUtil);
        try (var arena = Arena.ofConfined()) {
            luaL_loadstring(lua_State, arena.allocateFrom(luaCode));
            int iRet = lua_pcallk(lua_State, 0, 1, 0, 0L, NULL);
            // 获取lua代码中的runnable函数，开启线程执行
            int runnable = lua_getglobal(lua_State, arena.allocateFrom("runnable"));
            boolean iscfunction = lua_iscfunction(lua_State, -1) == 0;
            if (iscfunction) {
                new Thread(() -> lua_pcallk(lua_State, 0, 0, 0, 0L, NULL)).start();
                Thread.sleep(2000L);
            }
            if (iRet != 0) {
                printTopStackError(lua_State);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 关闭lua虚拟机
        lua_close(lua_State);
    }

    public static void printTopStackError(MemorySegment lua_State) {
        var luaType = lua_type(lua_State, -1);
        System.out.println("luaType = " + luaType);
        if (LUA_TSTRING() == luaType) {
            var data2 = lua_tolstring(lua_State, -1, NULL);
            var param2 = data2.getString(0L);
            System.out.println("lua error info = " + param2);
        }
    }

}
