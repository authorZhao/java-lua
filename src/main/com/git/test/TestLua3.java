package com.git.test;

import com.git.lua.lua_h;
import com.git.po.User;
import com.git.util.LuaMathUtil;
import com.git.util.LuaUtil;
import com.git.util.UserUtil;
import com.git.util.UserUtilV2;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.git.lua.lauxlib_h.luaL_loadfilex;
import static com.git.lua.lauxlib_h.luaL_newstate;
import static com.git.lua.lualib_h.luaL_openlibs;
import static java.lang.foreign.MemorySegment.NULL;

/**
 * @author authorZhao
 */
public class TestLua3 {

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {
        var lua_State = luaL_newstate();
        if (lua_State == NULL) {
            return;
        }
        // 打开内置库
        luaL_openlibs(lua_State);
        var luaUtil = new LuaUtil();
        LuaMathUtil.openJavaMath(lua_State, luaUtil);
        UserUtilV2.loadUser(lua_State, User.class, luaUtil);
        try (var arena = Arena.ofConfined()) {
            int loadResult = luaL_loadfilex(lua_State, arena.allocateFrom("E:/tool/lua5.4-zhao/test4.lua"), NULL);
            int iRet = lua_h.lua_pcallk(lua_State, 0, 1, 0, 0L, NULL);
            if (iRet != 0) {
                printTopStackError(lua_State);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 关闭lua虚拟机
        lua_h.lua_close(lua_State);
    }

    public static void printTopStackError(MemorySegment lua_State) {
        var luaType = lua_h.lua_type(lua_State, -1);
        System.out.println("luaType = " + luaType);
        if (lua_h.LUA_TSTRING() == luaType) {
            var data2 = lua_h.lua_tolstring(lua_State, -1, NULL);
            var param2 = data2.getString(0L);
            System.out.println("lua error info = " + param2);
        }
    }

}
