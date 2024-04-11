# ffm之HelloWorld

直接上代码

```java
/**
 * @author authorZhao
 * @since 2024-03-13
 */
public class FFMHelloWorld {
    public static void main(String[] args) {
        Linker linker = Linker.nativeLinker();
        SymbolLookup defaultLookup = linker.defaultLookup();
        //调用c语言的strlen和printf函数
        MethodHandle strlenHandle = linker.downcallHandle(
                defaultLookup.find("strlen").orElseThrow(),
                FunctionDescriptor.of(JAVA_LONG, ADDRESS));
        MethodHandle printfHandler = linker.downcallHandle(
                defaultLookup.find("printf").orElseThrow(),
                FunctionDescriptor.of(JAVA_LONG, ADDRESS,JAVA_INT));
        try (Arena offHeap = Arena.ofConfined()) {
            MemorySegment pointers = offHeap.allocateUtf8String("Hello world%d!");
            System.out.println(strlenHandle.invoke(pointers));  //11
            printfHandler.invoke(pointers,8);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
```



