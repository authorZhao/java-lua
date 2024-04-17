--package.cpath = package.cpath .. ';C:/Users/Admin/AppData/Roaming/JetBrains/IntelliJIdea2024.1/plugins/EmmyLua/debugger/emmy/windows/x64/?.dll'
--local dbg = require('emmy_core')
--dbg.tcpConnect('localhost', 9966)

local javaMath = require("JavaMath")

function printTable(table)
    for key, value in pairs(table) do
        print(key, value)
    end
end

for name, _ in pairs(package.loaded) do
    print("模块名称=" .. name)
end

function test()
    print("--------print table---------")
    printTable(javaMath)
    print(User)
    print("--------lua start create user---------")
    user = User()
    if(not user) then
        print("--------create user nil---------")
        return
    end
    print("--------lua print user start---------")
    print(user)
    print("--------lua print user end---------")
    user:setName("李四")
    user:setAge(65)
    user:setSex(3)
    print(user:toString())
    print("--------lua print user start---------")
    print(user)
    print("--------lua print user end---------")
    print("lua name = " .. user:getName())

    print(user:getName())
    print(math.max(1, 6.3))
    print("javaMath PI=" .. "" .. javaMath.PI)
    print("javaMath E=" .. "" .. javaMath.E)

    print("javaMath max=" .. "" .. javaMath.max(2, 1))
    print("javaMath random=" .. "" .. javaMath.random())



end

function runnable()
    print("渣渣辉开线程")
end

test()
