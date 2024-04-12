package com.git.test;

import com.git.util.UserUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.git.lua.luahpp_h.*;
import static java.lang.foreign.MemorySegment.NULL;

/**
 * @author authorZhao
 */
public class TestLua2 {

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {
        var lua_State = luaL_newstate();
        if (lua_State == NULL) {
            return;
        }
        // 打开内置库
        luaL_openlibs(lua_State);
        UserUtil.loadUser(lua_State);
        try (var arena = Arena.ofConfined()) {
            int loadResult = luaL_loadfilex(lua_State, arena.allocateFrom("E:/tool/lua5.4-zhao/test3.lua"), NULL);
            int iRet = lua_pcallk(lua_State, 0, 1, 0, 0L, NULL);
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
        if (LUA_TSTRING() == luaType) {
            var data2 = lua_tolstring(lua_State, -1, NULL);
            var param2 = data2.getString(0L);
            System.out.println("lua error info = " + param2);
        }
    }

}
