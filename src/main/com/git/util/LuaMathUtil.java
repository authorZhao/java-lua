package com.git.util;

import com.git.lua.lauxlib_h;
import com.git.lua.luaL_Reg;
import com.git.lua.lua_CFunction;
import com.git.lua.lua_h;
import com.git.util.obj.DestroyObject;
import com.git.util.obj.MethodObject;
import com.git.util.obj.NewObject;
import com.git.util.obj.TotringObject;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static com.git.lua.lauxlib_h.luaL_checkversion_;
import static com.git.lua.lauxlib_h.luaL_newmetatable;
import static com.git.lua.lauxlib_h.luaL_setfuncs;
import static com.git.lua.lauxlib_h.remove;
import static com.git.lua.lua_h.*;
import static java.lang.foreign.MemorySegment.NULL;

/**
 * @author authorZhao
 * @since 2024-03-28
 */
public class LuaMathUtil {
    public static void openJavaMath(MemorySegment L, LuaUtil luaUtil) {

        var luaopen_java_math = new lua_CFunction.Function() {
            @Override
            public int apply(MemorySegment luaState) {
                try (Arena arena = Arena.ofConfined()) {
                    List<Method> list = Arrays.stream(Math.class.getDeclaredMethods())
                            .filter(i -> Modifier.isPublic(i.getModifiers()))
                            .toList();
                    List<Field> fieldList = Arrays.stream(Math.class.getDeclaredFields())
                            .filter(i -> Modifier.isPublic(i.getModifiers()))
                            .toList();
                    luaL_checkversion_(luaState, lua_h.LUA_VERSION_NUM(), lauxlib_h.LUAL_NUMSIZES());
                    lua_h.lua_createtable(luaState, 0, list.size());

                    MemorySegment memorySegment = luaL_Reg.allocateArray(list.size() + fieldList.size() + 1, arena);
                    for (int j = 0; j < list.size(); j++) {
                        Method method = list.get(j);
                        MemorySegment slice = luaL_Reg.asSlice(memorySegment, j);
                        String name = method.getName();
                        luaL_Reg.name(slice, arena.allocateFrom(name));
                        luaL_Reg.func(slice, lua_CFunction.allocate(new MethodObject(method, luaUtil), Arena.ofAuto()));
                    }

                    for (int j = list.size(); j < list.size() + fieldList.size(); j++) {
                        Field field = fieldList.get(j - list.size());
                        MemorySegment slice = luaL_Reg.asSlice(memorySegment, j);
                        String name = field.getName();
                        luaL_Reg.name(slice, arena.allocateFrom(name));
                        luaL_Reg.func(slice, NULL);
                    }

                    MemorySegment slice3 = luaL_Reg.asSlice(memorySegment, list.size());
                    luaL_Reg.name(slice3, NULL);
                    luaL_Reg.func(slice3, NULL);

                    luaL_setfuncs(luaState, memorySegment, 0);

                    fieldList.forEach(i -> {
                        try {
                            lua_pushnumber(L, i.getDouble(null));
                            lua_setfield(L, -2, arena.allocateFrom(i.getName()));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                    });
                }
                return 1;
            }
        };

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment allocate = lua_CFunction.allocate(luaopen_java_math, arena);
            // luaL_newlib
            lauxlib_h.luaL_requiref(L, arena.allocateFrom("JavaMath"), allocate, 1);
            lua_settop(L, -2);
        }

    }
}
