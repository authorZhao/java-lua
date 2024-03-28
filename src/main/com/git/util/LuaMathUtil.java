package com.git.util;

import com.git.lua.luaL_Reg;
import com.git.lua.lua_CFunction;
import com.git.lua.lua_h;
import com.git.util.obj.DestroyObject;
import com.git.util.obj.MethodObject;
import com.git.util.obj.NewObject;
import com.git.util.obj.TotringObject;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.git.lua.lauxlib_h.luaL_newmetatable;
import static com.git.lua.lauxlib_h.luaL_setfuncs;
import static com.git.lua.lua_h.*;
import static com.git.lua.lua_h.lua_settable;
import static java.lang.foreign.MemorySegment.NULL;

/**
 * @author authorZhao
 * @since 2024-03-28
 */
public class LuaMathUtil {
    public static void openJavaMath(MemorySegment luaState,LuaUtil luaUtil){
        try (Arena arena = Arena.ofConfined()) {
            lua_h.lua_createtable(luaState, 0,0); // 创建一个元表
            lua_pushvalue(luaState, -1);  // 将新表的堆栈副本压入栈顶
            lua_setfield(luaState, lua_h.LUA_REGISTRYINDEX(), arena.allocateFrom("Math"));  // 将新表设置到注册表


            List<Method> list = Arrays.stream(Math.class.getDeclaredMethods()).toList();
            MemorySegment memorySegment = luaL_Reg.allocateArray(list.size() + 1, arena);

            for (int j = 0; j < list.size(); j++) {
                Method method = list.get(j);
                MemorySegment slice = luaL_Reg.asSlice(memorySegment, j);
                String name = method.getName();
                luaL_Reg.name(slice, arena.allocateFrom(name));
                luaL_Reg.func(slice, lua_CFunction.allocate(new MethodObject(method, luaUtil), Arena.ofAuto()));
            }

            MemorySegment slice3 = luaL_Reg.asSlice(memorySegment, list.size());
            luaL_Reg.name(slice3, NULL);
            luaL_Reg.func(slice3, NULL);

            luaL_setfuncs(luaState, memorySegment, 0);
        }
    }
}
