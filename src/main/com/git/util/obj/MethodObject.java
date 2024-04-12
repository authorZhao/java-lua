package com.git.util.obj;

import com.git.lua.lua_CFunction;
import com.git.util.LuaUtil;

import java.lang.foreign.MemorySegment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.git.lua.luahpp_h.lua_gettop;
import static com.git.lua.luahpp_h.lua_touserdata;


/**
 * 方法执行
 *
 * @author authorZhao
 */
public class MethodObject implements lua_CFunction.Function {
    // 考虑使用MethodHandle和ffm更加搭配
    private final Method method;
    private final int paramCount;
    private final LuaUtil luaUtil;
    private final boolean isStatic;

    public MethodObject(Method method, LuaUtil luaUtil) {
        this.method = method;
        this.luaUtil = luaUtil;
        this.paramCount = method.getParameterCount();
        isStatic = Modifier.isStatic(method.getModifiers());
        //System.out.println("method name = " + method.getName());
    }

    @Override
    public int apply(MemorySegment lua_State) {
        if (isStatic) {
            return doStatic(lua_State);
        }
        int cnt = lua_gettop(lua_State);
        if (cnt != paramCount + 1) {
            System.err.println("method parameter count mismatch = " + method.getName());
            return 0;
        }
        MemorySegment memorySegment = lua_touserdata(lua_State, 1);
        Object instance = luaUtil.getObjByAddress(memorySegment.address());
        if (instance == null) {
            return 0;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object object = luaUtil.getObject(lua_State, i + 2, parameterTypes[i]);
            params[i] = object;
        }
        Object invoke = null;
        try {
            invoke = method.invoke(instance, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        boolean b = method.getReturnType() == void.class || method.getReturnType() == Void.class;
        if (b) {
            return 0;
        }
        luaUtil.returnLua(invoke, lua_State);
        return 1;
    }

    private int doStatic(MemorySegment lua_State) {
        int cnt = lua_gettop(lua_State);
        if (cnt != paramCount) {
            System.err.println("static method parameter count mismatch = " + method.getName());
            return 0;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object object = luaUtil.getObject(lua_State, i + 1, parameterTypes[i]);
            params[i] = object;
        }
        Object invoke = null;
        try {
            invoke = method.invoke(null, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        boolean b = method.getReturnType() == void.class || method.getReturnType() == Void.class;
        if (b) {
            return 0;
        }
        luaUtil.returnLua(invoke, lua_State);
        return 1;
    }
}
