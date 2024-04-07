package com.git.util.handler;

import com.git.lua.lua_CFunction;
import com.git.util.LuaUtil;

import java.lang.foreign.MemorySegment;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 方法执行
 *
 * @author authorZhao
 */
public class OverrideMethodObject implements lua_CFunction.Function {
    // 考虑使用MethodHandle和ffm更加搭配
    private List<MethodHandlerObject> methodHandlerObjectList = new CopyOnWriteArrayList<>();

    public OverrideMethodObject(Class<?> clazz,List<Method> methods, LuaUtil luaUtil) {
        for (Method method : methods) {
            methodHandlerObjectList.add(MethodHandlerObject.newMethodHandle(clazz,method,luaUtil));
        }
        System.out.println("methods name = " + methods.getFirst().getName());
    }

    @Override
    public int apply(MemorySegment lua_State) {
        Optional<MethodHandlerObject> first = methodHandlerObjectList.stream().filter(i -> i.match(lua_State)).findFirst();
        return first.map(methodHandlerObject -> methodHandlerObject.apply(lua_State)).orElse(0);
    }
}
