# ffm之lua集成helloworld

## lua下载

注意需要下载源码最好，有条件的自行编译,编译为动态库 本文以windows为例(mingw、msvc都可)
![需要的lua头文件](https://opadmin.pingyuanren.top/file/png/2024/841614b5e3b149a1a8febbc3b8aafb40.png)

![lua动态库](https://opadmin.pingyuanren.top/file/png/2024/2dc5fdf7416840738d9c038c2293ab78.png)

效果如图,本文只需要dll即可，动态库名字我自己改的

头文件目录


## 代码生成
生成脚本

生成代码并，拷贝代码到idea

```
jextract --output src -t com.lua -I "E:/tool/lua-5.4.4/src" "E:/tool/lua-5.4.4/src/lua.h" -l :E:/tool/lua5.4-zhao/liblua5.4.0.dll

jextract --output src -t com.lua -I "E:/tool/lua-5.4.4/src" "E:/tool/lua-5.4.4/src/lualib.h" -l :E:/tool/lua5.4-zhao/liblua5.4.0.dll

jextract --output src -t com.lua -I "E:/tool/lua-5.4.4/src" "E:/tool/lua-5.4.4/src/lauxlib.h" -l :E:/tool/lua5.4-zhao/liblua5.4.0.dll
```

![生成操作图](https://opadmin.pingyuanren.top/file/png/2024/9677d50e519045eba35dff8f874a4fb2.png)

## 测试代码

### 1.lua代码
```lua
print("package.cpath=" .. package.cpath)
-- 注意cjson不在lua.exe同级目录，测试的时候已经移动下一级别再下一级别
package.cpath = package.cpath .. ';E:/tool/lua5.4-zhao/dll/?.dll'
local cjson = require("cjson")

function test()
    local map = {}
    map["a"] = "张三"
    map["b"] = "李四"
    for k,v in pairs(map) do
        print(k .. '=' .. v)
    end
    print(cjson.encode(map))
    return 100
end
test()
```
### 2.java代码

```java
/**
 * @author authorZhao
 */
public class LuaMain {
    static {
        //这里不适用load,生成代码使用-l指定动态库的位置，生成的代码直接包含了，但是这种方式用的是绝对路径，使用SymbolLookup.libraryLookup
        //System.load("E:/tool/lua5.4-zhao/liblua5.4.0.dll");
    }

    public static void main(String[] args) {
        var lua_State = luaL_newstate();
        if (lua_State == NULL) {
            return;
        }
        //打开内置库
        luaL_openlibs(lua_State);
        try (var arena = Arena.ofConfined()) {
            var code = arena.allocateUtf8String("print(\"渣渣辉\") return \"嘿嘿\"");
            //加载而不执行
            int i = luaL_loadstring(lua_State, code);
            if (i != 0) {
                return;
            }
            // pcall
            lua_h.lua_pcallk(lua_State, 0, 1, 0, 0L, NULL);
            var data2 = lua_h.lua_tolstring(lua_State, -1, NULL);
            System.out.println("data2 = " + data2.getUtf8String(0L));

            //这里的utf-8可能对中文路径有问题,英文影响应该不大
            int loadResult = luaL_loadfilex(lua_State, arena.allocateUtf8String("E:/tool/lua5.4-zhao/test2.lua"), NULL);
            System.out.println("loadResult = " + loadResult);

            // pcall
            int iRet = lua_h.lua_pcallk(lua_State, 0, 1, 0, 0L, NULL);

            if (iRet == 0) {
                var data = lua_h.lua_tointegerx(lua_State, -1, NULL);
                System.out.println("data = " + data);
            }

        }
        //关闭lua虚拟机
        lua_h.lua_close(lua_State);
    }
}
```

## 测试结果

![1.lua代码直接执行结果](https://opadmin.pingyuanren.top/file/png/2024/7b8dd4e5105b456885b59aea888e80ef.png)

![2.java代码执行结果](https://opadmin.pingyuanren.top/file/png/2024/8c5db829f1dc4103a4f1f0306b423c8e.png)
