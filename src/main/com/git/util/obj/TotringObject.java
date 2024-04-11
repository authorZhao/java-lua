package com.git.util.obj;

import com.git.lua.lua_CFunction;
import com.git.util.LuaUtil;

import java.lang.foreign.MemorySegment;

import static com.git.lua.lua_h.lua_touserdata;

/**
 * toString的实现
 * 方法执行
 *
 * @author authorZhao
 */
public class TotringObject implements lua_CFunction.Function {
    private LuaUtil luaUtil;

    public TotringObject(LuaUtil luaUtil) {
        this.luaUtil = luaUtil;
    }

    @Override
    public int apply(MemorySegment lua_State) {
        MemorySegment memorySegment = lua_touserdata(lua_State, 1);
        Object instance = luaUtil.getObjByAddress(memorySegment.address());
        if (instance == null) {
            return 0;
        }
        luaUtil.returnLua(instance.toString(), lua_State);
        return 1;
    }
}
