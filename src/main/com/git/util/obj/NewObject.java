package com.git.util.obj;

import com.git.lua.lua_CFunction;
import com.git.lua.luaconf_h;
import com.git.util.LuaUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import static com.git.lua.lua_h.*;
import static java.lang.foreign.ValueLayout.ADDRESS;

/**
 * 新建对象的实现
 * @author authorZhao
 * @since 2024-03-25
 */
public class NewObject implements lua_CFunction.Function {
    private Class<?> clazz;
    private LuaUtil luaUtil;

    public NewObject(Class<?> clazz, LuaUtil luaUtil) {
        this.clazz = clazz;
        this.luaUtil = luaUtil;
        System.out.println("Class name = " + clazz.getSimpleName());
    }

    @Override
    public int apply(MemorySegment lua_State) {
        int cnt = lua_gettop(lua_State);
        Constructor<?>[] constructors = clazz.getConstructors();
        Optional<Constructor<?>> first = Arrays.stream(constructors).filter(i -> cnt == i.getParameterCount()).findFirst();
        if (first.isEmpty()) {
            System.out.println("new Object Constructor isEmpty " + clazz.getName());
            return 0;
        }
        Class<?>[] parameterTypes = first.get().getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object object = luaUtil.getObject(lua_State, i + 1, parameterTypes[i]);
            params[i] = object;
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment allocate = lua_newuserdatauv(lua_State, ADDRESS.byteSize(), 1);
            Object o = first.get().newInstance(params);
            System.out.println("new Object java = " + o);
            luaUtil.putObj(allocate.address(), o);
            lua_getfield(lua_State, -luaconf_h.LUAI_MAXSTACK() - 1000, arena.allocateFrom(clazz.getSimpleName()));
            lua_setmetatable(lua_State, -2);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return 1;
    }
}
