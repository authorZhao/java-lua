package com.git.util.handler;

import com.git.lua.lua_CFunction;
import com.git.util.LuaUtil;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.git.lua.lua_h.lua_gettop;
import static com.git.lua.lua_h.lua_touserdata;

/**
 * 方法执行
 *
 * @author authorZhao
 */
public class MethodHandlerObject implements lua_CFunction.Function {
    // 考虑使用MethodHandle和ffm更加搭配
    private final MethodHandle methodHandle;
    private final int paramCount;
    private final LuaUtil luaUtil;
    private boolean isStatic = false;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;

    private MethodHandlerObject(MethodHandle methodHandle, int paramCount, LuaUtil luaUtil, boolean isStatic,
            String methodName, Class<?>[] parameterTypes, Class<?> returnType) {
        this.methodHandle = methodHandle;
        this.paramCount = paramCount;
        this.luaUtil = luaUtil;
        this.isStatic = isStatic;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        System.out.println("MethodHandlerObject method name = " + methodName);
    }

    @Override
    public int apply(MemorySegment lua_State) {
        System.out.println("MethodHandlerObject apply methodName =" + methodName);
        if (isStatic) {
            return doStatic(lua_State);
        }
        int cnt = lua_gettop(lua_State);
        if (cnt != paramCount + 1) {
            System.err.println("method parameter count mismatch = " + methodName);
            return 0;
        }
        MemorySegment memorySegment = lua_touserdata(lua_State, 1);
        Object instance = luaUtil.getObjByAddress(memorySegment.address());
        if (instance == null) {
            return 0;
        }
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object object = luaUtil.getObject(lua_State, i + 2, parameterTypes[i]);
            params[i] = object;
        }
        return invoke(lua_State, params);
    }

    private int invoke(MemorySegment lua_State, Object[] params) {
        Object invoke = null;
        try {
            if (params.length == 0) {
                invoke = methodHandle.invoke();
            } else {
                invoke = methodHandle.invokeWithArguments(params);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        boolean b = returnType == void.class || returnType == Void.class;
        if (b) {
            return 0;
        }
        luaUtil.returnLua(invoke, lua_State);
        return 1;
    }

    private int doStatic(MemorySegment lua_State) {
        int cnt = lua_gettop(lua_State);
        if (cnt != paramCount) {
            System.err.println("static method parameter count mismatch = " + methodName);
            return 0;
        }

        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object object = luaUtil.getObject(lua_State, i + 1, parameterTypes[i]);
            params[i] = object;
        }
        return invoke(lua_State, params);
    }

    public static MethodHandlerObject newMethodHandle(Class<?> clazz, Method method, LuaUtil luaUtil) {
        boolean isStatic = false;
        MethodHandle methodHandle = null;
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        try {
            if (Modifier.isStatic(method.getModifiers())) {
                isStatic = true;
                methodHandle = lookup.findStatic(clazz, method.getName(), methodType);
            } else {
                methodHandle = lookup.findSpecial(clazz, method.getName(), methodType, clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MethodHandlerObject(methodHandle, method.getParameterCount(), luaUtil, isStatic, method.getName(),
                method.getParameterTypes(), method.getReturnType());
    }

    public boolean match(MemorySegment luaState) {
        int cnt = lua_gettop(luaState);
        if (cnt != paramCount) {
            return false;
        }
        int index = 0;
        if (!isStatic) {
            index = 1;
        }
        for (int i = index; i < parameterTypes.length; i++) {
            boolean match = luaUtil.typeMatch(luaState, i, parameterTypes[i]);
            if (!match) {
                return false;
            }
        }
        return true;
    }
}
