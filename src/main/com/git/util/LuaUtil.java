package com.git.util;

import com.git.lua.lua_h;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.git.lua.lua_h.lua_gettop;
import static java.lang.foreign.MemorySegment.NULL;

/**
 * @author authorZhao
 * @since 2024-03-25
 */
public class LuaUtil {

    private final Map<Long, Object> MAP = new ConcurrentHashMap<>();
    private final Map<Object, MemorySegment> objectMemorySegmentMap = new ConcurrentHashMap<>();
    private final Arena arena = Arena.ofConfined();

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
        int type = lua_h.lua_type(lua_State, index);
        switch (type) {
            case 1 -> {
                return lua_h.lua_toboolean(lua_State, index);
            }
            case 3 -> {
                return lua_h.lua_tointegerx(lua_State, index, NULL);
            }
            case 4 -> {
                // string
                MemorySegment memorySegment = lua_h.lua_tolstring(lua_State, index, NULL);
                return memorySegment.getString(0L, StandardCharsets.UTF_8);
            }
            case 7 -> {
                // userdata
                MemorySegment memorySegment = lua_h.lua_touserdata(lua_State, index);
                return MAP.get(memorySegment.address());
            }
            default -> {
                return null;
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
            lua_h.lua_pushinteger(luaState, number.longValue());
        } else if (invoke instanceof Boolean number) {
            lua_h.lua_pushboolean(luaState, Boolean.TRUE.equals(number) ? 1 : 0);
        } else if (invoke instanceof String number) {
            // lua_h.lua_pushlstring(luaState, arena.allocateFrom(number,
            // StandardCharsets.UTF_8),number.getBytes(StandardCharsets.UTF_8).length);

            lua_h.lua_pushstring(luaState, arena.allocateFrom(number, StandardCharsets.UTF_8));
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
