package com.git.util;

import static com.git.lua.luahpp_h.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.foreign.MemorySegment.NULL;

/**
 * lua工具类，提供lua的指针与java对象的映射关系
 * @author authorZhao
 * @since 2024-03-25
 */
public class LuaUtil {
    private static final List<Class<?>> numClasses = List.of(
            int.class,
            float.class,
            double.class,
            byte.class,
            long.class,
            short.class,
            Integer.class,
            Float.class,
            Double.class,
            Byte.class,
            Long.class,
            Short.class,
            BigDecimal.class
    );
    private final Map<Long, Object> MAP = new ConcurrentHashMap<>();

    /**
     * #define LUA_TNONE (-1)
     * #define LUA_TNIL 0
     * #define LUA_TBOOLEAN 1
     * #define LUA_TLIGHTUSERDATA 2
     * #define LUA_TNUMBER 3
     * #define LUA_TSTRING 4
     * #define LUA_TTABLE 5
     * #define LUA_TFUNCTION 6
     * #define LUA_TUSERDATA 7
     * #define LUA_TTHREAD 8
     * #define LUA_NUMTYPES 9
     *
     * @param lua_State
     * @param index
     * @return
     */
    public Object getObject(MemorySegment lua_State, int index) {
        int type = lua_type(lua_State, index);
        switch (type) {
            case 1 -> {
                return lua_toboolean(lua_State, index);
            }
            case 3 -> {
                return lua_tointegerx(lua_State, index, NULL);
            }
            case 4 -> {
                // string
                MemorySegment memorySegment = lua_tolstring(lua_State, index, NULL);
                return memorySegment.getString(0L, StandardCharsets.UTF_8);
            }
            case 7 -> {
                // userdata
                MemorySegment memorySegment = lua_touserdata(lua_State, index);
                return MAP.get(memorySegment.address());
            }
            default -> {
                return null;
            }
        }
    }

    public boolean typeMatch(MemorySegment lua_State, int index,Class<?> classType) {
        int type = lua_type(lua_State, index);
        switch (type) {
            case 1 -> {
                return classType == boolean.class || classType == Boolean.class;
            }
            case 3 -> {
                if (lua_isinteger(lua_State, index) != 0) {
                    return classType == int.class || classType == Integer.class;
                }
                return numClasses.contains(classType);
            }
            case 4 -> {
                return classType == String.class;
            }
            default -> {
                return true;
            }
        }
    }

    public Object getObject(MemorySegment lua_State, int index, Class<?> requireType) {
        Object object = getObject(lua_State, index);
        if (object == null) {
            return null;
        }
        if (object instanceof Number number) {
            if (requireType == Integer.class || requireType == int.class) {
                return number.intValue();
            } else if (requireType == Float.class || requireType == float.class) {
                return number.floatValue();
            } else if (requireType == Double.class || requireType == double.class) {
                return number.doubleValue();
            } else if (requireType == Byte.class || requireType == byte.class) {
                return number.byteValue();
            } else if (requireType == Long.class || requireType == long.class) {
                return number.intValue();
            } else if (requireType == Short.class || requireType == short.class) {
                return number.shortValue();
            } else if (requireType == BigDecimal.class) {
                return BigDecimal.valueOf(number.longValue());
            } else {
                return null;
            }
        }
        return object;
    }

    public void returnLua(Object invoke, MemorySegment luaState) {
        if (invoke instanceof Number number) {
            if(invoke instanceof Double d){
                lua_pushnumber(luaState, d);
            }else {
                lua_pushinteger(luaState, number.longValue());
            }
        } else if (invoke instanceof Boolean number) {
            lua_pushboolean(luaState, Boolean.TRUE.equals(number) ? 1 : 0);
        } else if (invoke instanceof String number) {
            try (Arena arena = Arena.ofConfined()) {
                lua_pushstring(luaState, arena.allocateFrom(number, StandardCharsets.UTF_8));
            }
        }
    }

    public Object getObjByAddress(long address) {
        return MAP.get(address);
    }

    public Object removeObjByAddress(long address) {
        return MAP.remove(address);
    }

    public void putObj(long address, Object o) {
        MAP.put(address, o);
    }
}
