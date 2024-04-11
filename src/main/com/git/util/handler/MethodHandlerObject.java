package com.git.util.handler;

import com.git.lua.lua_CFunction;
import com.git.util.LuaUtil;

import java.lang.foreign.MemorySegment;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.git.lua.lua_h.lua_gettop;
import static com.git.lua.lua_h.lua_touserdata;

/**
 * <p>
 * 方法执行，为什么该类的构造函数设置为私有，主要是为了将MethodHandle设置为final，避免根据method再次解析
 * 相应的也提供了instance方法获取实例
 * </p>
 * <p>
 * 注意该实例仅仅用于静态方法调用，非静态方法发现会有问题
 * </p>
 * <p>
 * MethodHandle不能完全替代反射，在实例方法调用时，按照反射之调用参数对象是Object[]类型不对，调用失败
 * </p>
 *
 * @author authorZhao
 * @since 2024-04-10
 */
public class MethodHandlerObject implements lua_CFunction.Function {
    /**
     * 考虑使用MethodHandle和ffm更加搭配
     */
    private final MethodHandle methodHandle;
    /**
     * 参数个数
     */
    private final int paramCount;
    /**
     * lua参数转换工具列
     */
    private final LuaUtil luaUtil;

    /**
     * 类全名
     */
    private final String className;

    /**
     * 方法名
     */
    private final String methodName;

    /**
     * 参数类型
     */
    private final Class<?>[] parameterTypes;

    /**
     * 返回类型
     */
    private final Class<?> returnType;

    private MethodHandlerObject(MethodHandle methodHandle, int paramCount, LuaUtil luaUtil, String className,
                                String methodName, Class<?>[] parameterTypes, Class<?> returnType) {
        this.methodHandle = methodHandle;
        this.paramCount = paramCount;
        this.luaUtil = luaUtil;
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        System.out.println("MethodHandlerObject method name = " + methodName);
    }

    @Override
    public int apply(MemorySegment lua_State) {
        System.out.println("MethodHandlerObject apply claaName = " + className + ",methodName =" + methodName);
        int cnt = lua_gettop(lua_State);
        if (cnt != paramCount) {
            System.err.println("static method parameter count mismatch claaName = " + className + ",methodName =" + methodName);
            return 0;
        }
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object object = luaUtil.getObject(lua_State, i + 1, parameterTypes[i]);
            params[i] = object;
        }
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
        // 有返回类型进行返回
        luaUtil.returnLua(invoke, lua_State);
        return 1;
    }

    public static MethodHandlerObject newMethodHandle(Class<?> clazz, Method method, LuaUtil luaUtil) {
        MethodHandle methodHandle = null;
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        try {
            if (Modifier.isStatic(method.getModifiers())) {
                methodHandle = lookup.findStatic(clazz, method.getName(), methodType);
            } else {
                throw new UnmodifiableClassException(clazz.getName() + "#" + method.getName() + "非静态方法不支持");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MethodHandlerObject(methodHandle, method.getParameterCount(), luaUtil, clazz.getName(),
                method.getName(),
                method.getParameterTypes(), method.getReturnType());
    }

    public boolean match(MemorySegment luaState) {
        int cnt = lua_gettop(luaState);
        if (cnt != paramCount) {
            return false;
        }
        int index = 0;
        for (int i = index; i < parameterTypes.length; i++) {
            boolean match = luaUtil.typeMatch(luaState, i, parameterTypes[i]);
            if (!match) {
                return false;
            }
        }
        return true;
    }
}
