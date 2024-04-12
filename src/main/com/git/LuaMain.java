package com.git;


import com.git.lua.lua_CFunction;
import com.git.lua.lua_KFunction;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static com.git.lua.luahpp_h.*;
import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.*;


/**
 * @author authorZhao
 */
public class LuaMain {

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {
        Linker linker = Linker.nativeLinker();
        MethodHandle lua_KFunction22
                = MethodHandles.lookup()
                .findStatic(LuaMain.class, "hand_Lua_KFunction",
                        MethodType.methodType(int.class, MemorySegment.class,
                                int.class,
                                long.class));

        MemorySegment errorCallback
                = linker.upcallStub(lua_KFunction22,
                FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_LONG),
                Arena.ofAuto());

        var lua_State = luaL_newstate();
        if (lua_State == NULL) {
            return;
        }
        //打开内置库
        luaL_openlibs(lua_State);
        try (var arena = Arena.ofConfined()) {
            var code = arena.allocateFrom("print(\"渣渣辉\") return \"嘿嘿\"");
            //加载而不执行
            int i = luaL_loadstring(lua_State, code);
            if (i != 0) {
                return;
            }

            // pcall
            lua_pcallk(lua_State, 0, 1, 0, 0L, NULL);
            var data2 = lua_tolstring(lua_State, -1, NULL);
            //lua_settop(lua_State,-2);
            System.out.println("data2 = " + data2.getString(0L));
            //这里的utf-8可能对中文路径有问题,英文影响应该不大
            int loadResult = luaL_loadfilex(lua_State, arena.allocateFrom("E:/tool/lua5.4-zhao/test2.lua"), NULL);
            System.out.println("loadResult = " + loadResult);

            // pcall
            var callback = lua_KFunction.allocate(new My_Lua_KFunction(), Arena.ofAuto());
            //lua_pushcclosure(lua_State, callback, 0);
            // 这里虽然指定了callback，但是errorFun指定可能有问题，目前无法回到错误函数
            lua_pushcclosure(lua_State, lua_CFunction.allocate(new My_Lua_CFunction(), Arena.ofAuto()), 0);
            lua_setglobal(lua_State, arena.allocateFrom("upcall_java"));

            lua_pushcclosure(lua_State, lua_CFunction.allocate(new My_Lua_CFunction2(), Arena.ofAuto()), 0);
            lua_setglobal(lua_State, arena.allocateFrom("upcall_java_2"));



            //lua_callk(lua_State, 0, 1, 0, callback);
            int iRet = lua_pcallk(lua_State, 0, 1, 0, 0L, NULL);
            //printTopStackError(lua_State);
            if (iRet == 0) {
                var data = lua_tointegerx(lua_State, -1, NULL);
                System.out.println("data = " + data);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        //关闭lua虚拟机
        lua_close(lua_State);


    }

    static class My_Lua_KFunction implements lua_KFunction.Function {
        @Override
        public int apply(MemorySegment lua_State, int status, long ctx) {
            System.out.println("正在执行lua的java回调status=" + status + "lua_KContext="+ctx);
            var luaType = lua_type(lua_State, -1);
            int top = lua_gettop(lua_State);
            System.out.println("top = " + top);
            System.out.println("luaType = " + luaType);
            if(LUA_TSTRING() == luaType) {
                var data2 = lua_tolstring(lua_State, -1, NULL);
                var param2 = data2.getString(0L);
                System.out.println("param2 = " + param2);
            }

            return 0;
        }
    }

    static class My_Lua_CFunction2 implements lua_CFunction.Function {
        @Override
        public int apply(MemorySegment lua_State) {
            var param1 = lua_tointegerx(lua_State, 1, NULL);
            var data2 = lua_tolstring(lua_State, 2, NULL);
            var param2 = data2.getString(0L);
            var result = param2 + param1;
            var arena = Arena.ofAuto();
            lua_pushlstring(lua_State, arena.allocateFrom(result), result.getBytes().length);
            return 1;
        }
    }

    static class My_Lua_CFunction implements lua_CFunction.Function {
        @Override
        public int apply(MemorySegment lua_State) {
            var result = "古田乐";
            var arena = Arena.ofAuto();
            lua_pushlstring(lua_State, arena.allocateFrom(result), result.getBytes().length);
            System.out.println("正在执行My_Lua_CFunction result="+result);
            return 1;
        }
    }

    static class NewUserCFunction implements lua_CFunction.Function {
        @Override
        public int apply(MemorySegment lua_State) {
            System.out.println("正在执行My_Lua_CFunction");
            lua_pushinteger(lua_State, 5);
            return 1;
        }
    }


    public static int hand_Lua_KFunction(MemorySegment L, int status, long ctx) {
        System.out.println("正在执行lua的java手动回调status=" + status + "lua_KContext=" + ctx);
        return 0;
    }

    public static void printTopStackError(MemorySegment lua_State){
        var luaType = lua_type(lua_State, -1);
        System.out.println("luaType = " + luaType);
        if(LUA_TSTRING() == luaType) {
            var data2 = lua_tolstring(lua_State, -1, NULL);
            var param2 = data2.getString(0L);
            System.out.println("lua error info = " + param2);
        }
    }


}
