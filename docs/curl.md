# ffm之curl

## 一、 工具下载

 - [下载jextract](https://jdk.java.net/jextract/)   本文选择jdk22的版本

## 二、 参考github
 - [打开jextract的github链接](https://github.com/openjdk/jextract)

打开curl的例子 https://github.com/openjdk/jextract/tree/master/samples/libcurl

## 三、代码生成
### 1.windows脚本如下

```
param(
  [Parameter(Mandatory=$true, HelpMessage="The path to the lib curl installation")]
  [string]$curlpath
)

jextract `
  -I "$curlpath\include" `
  -I "$curlpath\include\curl" `
  --dump-includes 'includes_all.conf' `
  "$curlpath\include\curl\curl.h"
  
Select-String -Path 'includes_all.conf' -Pattern '(curl|sockaddr )' | %{ $_.Line } | Out-File -FilePath 'includes_filtered.conf' -Encoding ascii

jextract `
  --output src `
  -t org.jextract `
  -I "$curlpath\include" `
  -I "$curlpath\include\curl" `
  -llibcurl `
  '@includes_filtered.conf' `
  "$curlpath\include\curl\curl.h"

javac -d classes (ls -r src/*.java)
```



### 2.生成脚本
本人以自己的curl为例
```
本人的curlpath = E:/tool/curl-8.6.0_6-win64-mingw/curl-8.6.0_6-win64-mingw

我按照github执行了两次命令

1. 这个执行完毕得到一个includes_filtered.conf文件
jextract --output src -t org.jextract -I "E:/tool/curl-8.6.0_6-win64-mingw/curl-8.6.0_6-win64-mingw/include" -I "E:/tool/curl-8.6.0_6-win64-mingw/curl-8.6.0_6-win64-mingw/include/curl" -llibcurl '@includes_filtered.conf' "E:/tool/curl-8.6.0_6-win64-mingw/curl-8.6.0_6-win64-mingw/include/curl/curl.h"

2.这个执行完毕直接得到生成的java代
jextract --output src -t org.jextract -I "E:/tool/curl-8.6.0_6-win64-mingw/curl-8.6.0_6-win64-mingw/include" -I "E:/tool/curl-8.6.0_6-win64-mingw/curl-8.6.0_6-win64-mingw/include/curl" "E:/tool/curl-8.6.0_6-win64-mingw/curl-8.6.0_6-win64-mingw/include/curl/curl.h"
```

### 3.拷贝生成java代码(好家伙52M)到idea

生成的工具有一个方法和我本地jdk有点不一致(可能是版本原因，本地jdk21，工具依赖的jdk是22)， ctrl+shift+r 全局替换一下getUtf8String

重写本文的时候，已经知道了，jdk21的api叫getUtf8String，jdk22的就是allocateFrom，所以不用动生成的代码
```
arena.allocateFrom(urlStr); 替换为  arena.getUtf8String(urlStr);
```
## 四、测试代码运行

```java
import java.lang.foreign.Arena;
import static java.lang.foreign.MemorySegment.NULL;
import static org.jextract.curl_h.*;

public class CurlMain {
    static {
        //生成代码的时候没有指定动态库位置，暂时写死
        System.load("E:\\tool\\curl-8.6.0_6-win64-mingw\\curl-8.6.0_6-win64-mingw\\bin\\libcurl-x64.dll");
    }
   public static void main(String[] args) {
       var urlStr = "https://www.baidu.com";
       curl_global_init(CURL_GLOBAL_DEFAULT());
       var curl = curl_easy_init();
       //curl_easy_setopt
       if (!curl.equals(NULL)) {
           try (var arena = Arena.ofConfined()) {
               var url = arena.allocateUtf8String(urlStr);
               //忽略ssl忽略证书检查
               curl_easy_setopt.makeInvoker(C_LONG_LONG).apply(curl, CURLOPT_SSL_VERIFYPEER(), 0);
               curl_easy_setopt.makeInvoker(C_LONG_LONG).apply(curl, CURLOPT_URL(), url.address());
               int res = curl_easy_perform(curl);
               if (res != CURLE_OK()) {
                   String error = curl_easy_strerror(res).getUtf8String(0);
                   System.out.println("Curl error: " + error);
                   curl_easy_cleanup(curl);
               }
           }
       }
       curl_global_cleanup();
   }
}
```
curl的java执行结果

![image-20240313183126904](https://opadmin.pingyuanren.top/file/png/2024/3c2ffb41cf114b969e03b31987eef34c.png)
