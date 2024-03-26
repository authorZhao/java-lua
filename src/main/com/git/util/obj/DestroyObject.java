package com.git.util.obj;

import com.git.lua.lua_CFunction;
import com.git.util.LuaUtil;

import java.lang.foreign.MemorySegment;

import static com.git.lua.lua_h.lua_touserdata;

/**
 * 销毁方法
 * 
 * @author authorZhao
 */
public class DestroyObject implements lua_CFunction.Function {
    private LuaUtil luaUtil;

    public DestroyObject(LuaUtil luaUtil) {
        this.luaUtil = luaUtil;
    }

    @Override
    public int apply(MemorySegment lua_State) {
        MemorySegment memorySegment = lua_touserdata(lua_State, 1);
        Object remove = luaUtil.removeObjByAddress(memorySegment.address());
        System.out.println("lua_gc = " + remove);
        return 0;
    }
}
