package com.git.util;

import com.git.lua.*;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import static com.git.lua.lauxlib_h.luaL_newmetatable;
import static com.git.lua.lauxlib_h.luaL_setfuncs;
import static com.git.lua.lua_h.*;
import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

/**
 * @author authorZhao
 * @since 2024-03-19
 */
public class UserUtil {
    private static final Linker linker = Linker.nativeLinker();
    public static final String USER = "User";
    public static void loadUser(MemorySegment lua_State) {
        try (Arena arena = Arena.ofConfined()) {
            lua_h.lua_pushcclosure(lua_State, lua_CFunction.allocate(new NewUser(), Arena.ofAuto()), 0);
            lua_h.lua_setglobal(lua_State, arena.allocateFrom(USER));//从堆栈上弹出一个值，并将其设为全局变量 name 的新值。，这个值就是，CreatePerson
            //如果注册表中已存在键 tname，返回 0 。 否则， 为用户数据的元表创建一张新表。 向这张表加入 __name = tname 键值对， 并将 [tname] = new table 添加到注册表中， 返回 1 。 （__name项可用于一些错误输出函数。）

            //这两种情况都会把最终的注册表中关联 tname 的值压栈。

            int i = luaL_newmetatable(lua_State, arena.allocateFrom(USER));           // 创建一个元表

            //把栈上给定索引处的元素作一个副本压栈。
            lua_pushvalue(lua_State, -1);

            lua_pushstring(lua_State, arena.allocateFrom("__gc"));
            // 垃圾回收，将指针 s 指向的零结尾的字符串压栈。 因此 s 处的内存在函数返回后，可以释放掉或是立刻重用于其它用途。
            lua_h.lua_pushcclosure(lua_State, lua_CFunction.allocate(new DestroyUser(), Arena.ofAuto()), 0);
            //做一个等价于 t[k] = v 的操作， 这里 t 是给出的索引处的值， v 是栈顶的那个值， k 是栈顶之下的值。
            //这个函数会将键和值都弹出栈。 跟在 Lua 中一样，这个函数可能触发一个 "newindex" 事件的元方法 （参见 §2.4）。
            lua_settable(lua_State, -3);//此时-3位置应该是  v=CreatePerson , k=__gc

            lua_pushstring(lua_State, arena.allocateFrom("__index"));
            lua_pushvalue(lua_State, -2);                           // 注意这一句，其实是将__index设置成元表自己
            lua_settable(lua_State, -3);

            MemorySegment memorySegment = luaL_Reg.allocateArray(4, arena);

            MemorySegment slice = luaL_Reg.asSlice(memorySegment, 0);
            luaL_Reg.name(slice,arena.allocateFrom("getName"));
            luaL_Reg.func(slice,lua_CFunction.allocate(new GetName(), Arena.ofAuto()));

            MemorySegment slice1 = luaL_Reg.asSlice(memorySegment, 1);
            luaL_Reg.name(slice1,arena.allocateFrom("print"));
            luaL_Reg.func(slice1,lua_CFunction.allocate(new PrintUser(), Arena.ofAuto()));

            MemorySegment slice2 = luaL_Reg.asSlice(memorySegment, 2);
            luaL_Reg.name(slice2,arena.allocateFrom("setName"));
            luaL_Reg.func(slice2,lua_CFunction.allocate(new SetName(), Arena.ofAuto()));

            MemorySegment slice3 = luaL_Reg.asSlice(memorySegment, 3);
            luaL_Reg.name(slice3,NULL);
            luaL_Reg.func(slice3,NULL);

            luaL_setfuncs(lua_State, memorySegment, 0);
        }
    }

    static class GetName implements lua_CFunction.Function {
        @Override
        public int apply(MemorySegment lua_State) {
            int cnt = lua_gettop (lua_State);
            MemorySegment memorySegment = lua_touserdata(lua_State, 1);
            //MemoryLayout memoryLayout = MemoryLayout.structLayout(JAVA_LONG.withName("age"), ADDRESS.withName("name"), JAVA_LONG.withName("sex"));
            long l = memorySegment.get(JAVA_LONG, 0L);

            String name = memorySegment.getString(8L);
            System.out.println("name = " + name);
            lua_h.lua_pushstring(lua_State, memorySegment.asSlice(8L, ADDRESS));
            return 1;
        }
    }

    static class SetName implements lua_CFunction.Function {
        @Override
        public int apply(MemorySegment lua_State) {
            int cnt = lua_gettop (lua_State);
            MemorySegment memorySegment = lua_touserdata(lua_State, 1);
            MemorySegment memorySegment1 = lua_tolstring(lua_State, 2, NULL);
            String string = memorySegment1.getString(0L);
            System.out.println("cnt=" + cnt  + "string = " + string);
            memorySegment.setString( 8L, string);
            return 0;
        }
    }

    static class PrintUser implements lua_CFunction.Function {
        @Override
        public int apply(MemorySegment lua_State) {
            int cnt = lua_gettop (lua_State);
            MemorySegment memorySegment = lua_touserdata(lua_State, 1);
            //MemoryLayout memoryLayout = MemoryLayout.structLayout(JAVA_LONG.withName("age"), ADDRESS.withName("name"), JAVA_LONG.withName("sex"));
            long age = memorySegment.get(JAVA_LONG, 0L);
            String name = memorySegment.getString(8L,StandardCharsets.UTF_8);
            long sex = memorySegment.get(JAVA_LONG, 16L);
            System.out.println("PrintUser age="+age+",name="+name+",sex="+sex);
            return 0;
        }
    }

    static class DestroyUser implements lua_CFunction.Function {
        @Override
        public int apply(MemorySegment lua_State) {
            MemorySegment memorySegment = lua_touserdata(lua_State, 1);
            //MemoryLayout memoryLayout = MemoryLayout.structLayout(JAVA_LONG.withName("age"), ADDRESS.withName("name"), JAVA_LONG.withName("sex"));
            long age = memorySegment.get(JAVA_LONG, 0L);
            System.out.println("atIndex = " + memorySegment.address());
            String name = memorySegment.getString(8L);
            long sex = memorySegment.get(JAVA_LONG, 16L);
            System.out.println("DestroyUser age="+age+",name="+name+",sex="+sex);
            return 0;
        }
    }

    static class NewUser implements lua_CFunction.Function {
        @Override
        public int apply(MemorySegment lua_State) {
            var age = lua_h.lua_tointegerx(lua_State, 1, NULL);
            var elem1 = lua_h.lua_tolstring(lua_State, 2, NULL);
            var sex = lua_h.lua_tointegerx(lua_State, 3, NULL);
            String name = elem1.getString(0L);

            MemoryLayout memoryLayout = MemoryLayout.structLayout(JAVA_LONG.withName("age"), ADDRESS.withName("name"), JAVA_LONG.withName("sex"));
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment allocate = lua_newuserdatauv(lua_State, memoryLayout.byteSize(), 1);
                allocate.set(JAVA_LONG,0L,age);
                allocate.setString(8L,name);
                allocate.set(JAVA_LONG,16L,sex);
                int type = lua_getfield(lua_State, -luaconf_h.LUAI_MAXSTACK() - 1000, arena.allocateFrom(USER));
                int i = lua_setmetatable(lua_State, -2);

                //*(Person**)lua_newuserdata(l, sizeof(Person*)) = new Person();//-1
                //int i = luaL_getmetatable(l, "Person");//将注册表中 tname 对应的元表 （参见 luaL_newmetatable）压栈。 如果没有 tname 对应的元表，则将 nil 压栈并返回假//-2
                //i = lua_setmetatable(l, -2);//把一张表弹出栈，并将其设为给定索引处的值的元表


            }
            System.out.println("NewUser age="+age+",name="+name+",sex="+sex);
            return 1;
        }
    }
}
