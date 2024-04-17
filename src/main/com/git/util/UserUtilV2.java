package com.git.util;

import com.git.lua.*;
import com.git.util.obj.DestroyObject;
import com.git.util.obj.MethodObject;
import com.git.util.obj.NewObject;
import com.git.util.obj.TotringObject;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.git.lua.luahpp_h.*;
import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

/**
 * @author authorZhao
 * @since 2024-03-19
 */
public class UserUtilV2 {

    public static void loadUser(MemorySegment lua_State, Class<?> clazz, LuaUtil luaUtil) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment allocate1 = lua_CFunction.allocate(new NewObject(clazz, luaUtil), Arena.ofAuto());
            //System.out.println("userUtilV2 methodName = User,allocate = " + allocate1);

            lua_pushcclosure(lua_State, allocate1, 0);
            lua_setglobal(lua_State, arena.allocateFrom(clazz.getSimpleName()));// 从堆栈上弹出一个值，并将其设为全局变量 name

            int i = luaL_newmetatable(lua_State, arena.allocateFrom(clazz.getSimpleName())); // 创建一个元表
            // lua_pushvalue(lua_State, -1);

            lua_pushstring(lua_State, arena.allocateFrom("__gc"));
            lua_pushcclosure(lua_State, lua_CFunction.allocate(new DestroyObject(luaUtil), Arena.ofAuto()), 0);
            lua_settable(lua_State, -3);// k=__gc

            lua_pushstring(lua_State, arena.allocateFrom("__tostring"));
            lua_pushcclosure(lua_State, lua_CFunction.allocate(new TotringObject(luaUtil), Arena.ofAuto()), 0);
            lua_settable(lua_State, -3);

            lua_pushstring(lua_State, arena.allocateFrom("__index"));
            lua_pushvalue(lua_State, -2);
            lua_settable(lua_State, -3);

            List<Method> list = Arrays.stream(clazz.getDeclaredMethods()).toList();
            MemorySegment memorySegment = luaL_Reg.allocateArray(list.size() + 1, arena);

            for (int j = 0; j < list.size(); j++) {
                Method method = list.get(j);
                MemorySegment slice = luaL_Reg.asSlice(memorySegment, j);
                String name = method.getName();
                luaL_Reg.name(slice, arena.allocateFrom(name));
                MemorySegment allocate = lua_CFunction.allocate(new MethodObject(method, luaUtil), Arena.ofAuto());
                //System.out.println("userUtilV2 methodName=" + name +",allocate = " + allocate);
                luaL_Reg.func(slice, allocate);
            }

            MemorySegment slice3 = luaL_Reg.asSlice(memorySegment, list.size());
            luaL_Reg.name(slice3, NULL);
            luaL_Reg.func(slice3, NULL);

            luaL_setfuncs(lua_State, memorySegment, 0);
            lua_settop(lua_State,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
