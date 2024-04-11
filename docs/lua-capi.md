# lua-cpai

## lua之cpi熟悉

## 1.1 全局函数注册

以luaL_openlibs为例，该函数是lua默认模块的加载

```c
/*
** these libs are loaded by lua.c and are readily available to any Lua
** program
*/
static const luaL_Reg loadedlibs[] = {
  {LUA_GNAME, luaopen_base},
  {LUA_LOADLIBNAME, luaopen_package},
  {LUA_COLIBNAME, luaopen_coroutine},
  {LUA_TABLIBNAME, luaopen_table},
  {LUA_IOLIBNAME, luaopen_io},
  {LUA_OSLIBNAME, luaopen_os},
  {LUA_STRLIBNAME, luaopen_string},
  {LUA_MATHLIBNAME, luaopen_math},
  {LUA_UTF8LIBNAME, luaopen_utf8},
  {LUA_DBLIBNAME, luaopen_debug},
  {NULL, NULL}
};


LUALIB_API void luaL_openlibs (lua_State *L) {
  const luaL_Reg *lib;
  /* "require" functions from 'loadedlibs' and set results to global table */
  for (lib = loadedlibs; lib->func; lib++) {
    luaL_requiref(L, lib->name, lib->func, 1);
    //lua_pop是一个宏展开就是 lua_settop(L, -2)
    lua_pop(L, 1);  /* remove lib */
    
  }
}
```

## 以以utf8模块为例

```c
//需要注册的函数
static const luaL_Reg funcs[] = {
  {"offset", byteoffset},
  {"codepoint", codepoint},
  {"char", utfchar},
  {"len", utflen},
  {"codes", iter_codes},
  /* placeholders */
  {"charpattern", NULL},
  {NULL, NULL}
};


LUAMOD_API int luaopen_utf8 (lua_State *L) {
    //是一个嵌套的宏，展开如下
  luaL_newlib(L, funcs);
   
    //设置字段，往utf8这个表里面设置charpattern字段，前面有占位
  lua_pushlstring(L, UTF8PATT, sizeof(UTF8PATT)/sizeof(char) - 1);
  lua_setfield(L, -2, "charpattern");
    
  return 1;
}


#define luaL_newlib(L, l)  \ (luaL_checkversion(L), luaL_newlibtable(L,l), luaL_setfuncs(L,l,0))
// Replacement:  
//(luaL_checkversion_(L, 504, (sizeof(lua_Integer) * 16 + sizeof(lua_Number))), lua_createtable(L, 0, sizeof(funcs) /sizeof((funcs)[0]) -1), luaL_setfuncs(L,funcs,0))

//核心就是如下
luaL_checkversion_(L,504,136) //其中504和136是在x64机器上的结果
lua_createtable(L,0,函数数组的长度) //c语言的数组长度求取方法
luaL_setfuncs(L,funcs,0)//注册函数
```


# 其他参考

lua5.4文档 https://www.lua.org/manual/5.4/

lua5.3中文文档(这里面对capi翻译的很详细) https://www.runoob.com/manual/lua53doc/manual.html#lua_tolstring
