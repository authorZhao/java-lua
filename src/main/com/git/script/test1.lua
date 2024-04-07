local javaMath = require("JavaMath")

function printTable(table)
    for key, value in pairs(table) do
        print(key, value)
    end
end

function test()
    print("--------print table---------")
    printTable(javaMath)

    print("--------lua start create user---------")
    user = User()
    if(not user) then
        print("--------create user nil---------")
        return
    end
    print("--------lua print user start---------")
    print(user)
    print("--------lua print user end---------")
    user:setName("张三")
    user:setAge(20)
    user:setSex(3)
    print(user:toString())
    print("--------lua print user start---------")
    print(user)
    print("--------lua print user end---------")
    print("lua name = " .. user:getName())

    print(user:getName())
    print(math.max(1, 6.3))
    print("javaMath max=" .. "" .. javaMath.max(3, 1))
    print("javaMath random=" .. "" .. javaMath.random())
    print("javaMath PI=" .. "" .. javaMath.PI)
    print("javaMath E=" .. "" .. javaMath.E)


end

function runnable()
    print("渣渣辉开线程")
end

test()
