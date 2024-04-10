package com.git.util;

import com.git.lua.lauxlib_h;
import com.git.lua.luaL_Reg;
import com.git.lua.lua_CFunction;
import com.git.lua.lua_h;
import com.git.util.obj.MethodObject;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.git.lua.lauxlib_h.luaL_checkversion_;
import static com.git.lua.lauxlib_h.luaL_setfuncs;
import static com.git.lua.lua_h.*;
import static java.lang.foreign.MemorySegment.NULL;

/**
 * @author authorZhao
 * @since 2024-03-28
 */
public class LuaMathUtil {
    private static final Class<Math> MATH_CLASS = Math.class;

    public static void openJavaMath(MemorySegment L, LuaUtil luaUtil) {

        var luaopen_java_math = new lua_CFunction.Function() {
            @Override
            public int apply(MemorySegment luaState) {
                try (Arena arena = Arena.ofConfined()) {
                    Map<String, List<Method>> methodMap = Arrays.stream(MATH_CLASS.getDeclaredMethods())
                            .filter(i -> Modifier.isPublic(i.getModifiers()) && Modifier.isStatic(i.getModifiers()))
                            .sorted(Comparator.comparing(Method::getName))
                            .collect(Collectors.groupingBy(Method::getName));

                    List<Field> fieldList = Arrays.stream(MATH_CLASS.getDeclaredFields())
                            .filter(i -> Modifier.isPublic(i.getModifiers()))
                            .toList();
                    luaL_checkversion_(luaState, lua_h.LUA_VERSION_NUM(), lauxlib_h.LUAL_NUMSIZES());
                    lua_h.lua_createtable(luaState, 0, methodMap.size());

                    MemorySegment memorySegment = luaL_Reg.allocateArray(methodMap.size() + fieldList.size() + 1,
                            arena);

                    int j = 0;
                    for (Map.Entry<String, List<Method>> entry : methodMap.entrySet()) {
                        List<Method> methods = entry.getValue();
                        String name = entry.getKey();
                        Method method = methods.getFirst();
                        if ("max".equals(method.getName())) {
                            method = methods.stream().filter(i -> i.getParameterTypes().length > 0 && i.getParameterTypes()[0] == int.class).findFirst().orElse(method);
                        }
                        MemorySegment slice = luaL_Reg.asSlice(memorySegment, j);
                        luaL_Reg.name(slice, arena.allocateFrom(name));
                        //var callback = MethodHandlerObject.newMethodHandle(MATH_CLASS, method, luaUtil);
                        var callback = new MethodObject(method,luaUtil);
                        MemorySegment allocate = lua_CFunction.allocate(callback, Arena.ofAuto());
                        System.out.println("methodName allocate = " + method.getName() + ":" + allocate);
                        luaL_Reg.func(slice, allocate);
                        j++;
                    }
                    for (j = methodMap.size(); j < methodMap.size() + fieldList.size(); j++) {
                        Field field = fieldList.get(j - methodMap.size());
                        MemorySegment slice = luaL_Reg.asSlice(memorySegment, j);
                        String name = field.getName();
                        luaL_Reg.name(slice, arena.allocateFrom(name));
                        luaL_Reg.func(slice, NULL);
                    }

                    MemorySegment slice3 = luaL_Reg.asSlice(memorySegment, methodMap.size());
                    luaL_Reg.name(slice3, NULL);
                    luaL_Reg.func(slice3, NULL);

                    luaL_setfuncs(luaState, memorySegment, 0);

                    fieldList.forEach(i -> {
                        try {
                            lua_pushnumber(L, i.getDouble(null));
                            lua_setfield(L, -2, arena.allocateFrom(i.getName()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
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
