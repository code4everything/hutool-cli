## 简介

顾名思义，这是一个可以在终端执行的 hutool。

那 hutool-cli 可以做什么呢？

首先Hutool是一个非常好用的Java工具库，提供了非常多地静态工具方法供大家使用，极大地提高了我们日常的开发效率；
但是如果我们想要快速的知道一个方法的运行效果，或者是调试一些方法，亦或者是想利用一些工具方法生成一些内容并复制它到其他应用程序，那我们就不得不写个测试类，然后再调一下对应的方法，最后查看执行结果，但这未免显得过于费劲。

于是 hutool-cli 把 hutool 带到了终端中，让我们可以直接通过命令去执行一个静态的工具方法，让日常的开发变得更有效率。

比如我们想生成一个随机UUID，现在只需要打开终端执行下面命令：

```shell
hu uuid
# output: 483cc7fc-4b22-4188-8f1c-dc1ce4b6d3ee
```

现在不仅仅是终端里的Hutool了，更多功能等待你来探索哦。

## 内置指令

查看时间进度

```shell
hu dayp

# output:
辛丑牛年 五月初三 周六 2021-06-12 09:45:07.755

今日 [oooooooooooooooooooooooooooooooooooooooo                                                            ]: 40.63%
本周 [oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo                        ]: 76.79%
本月 [oooooooooooooooooooooooooooooooooooooooo                                                            ]: 40.00%
本年 [oooooooooooooooooooooooooooooooooooooooooooo                                                        ]: 44.66%
```

列出目录文件

```shell
hu ls

# output:
2021-05-30 00:46:38	      0	bin
2021-06-02 19:48:08	      0	external
2021-06-12 10:29:19	   4 KB	method
2021-06-25 19:26:38	4.21 KB	class.json
2021-06-25 19:27:11	8.06 KB	command.json
2021-06-12 10:29:19	  749 B	converter.json
2021-06-09 22:19:07	  165 B	external.conf
2021-06-24 22:15:32	 7.2 MB	hutool.jar
```

查看日历

```shell
hu calendar 202106

# output:
     2021-06-25    
Mo Tu We Th Fr Sa Su
    1  2  3  4  5  6
 7  8  9 10 11 12 13
14 15 16 17 18 19 20
21 22 23 24 25 26 27
28 29 30               

# also support like follow command: 
# hu calendar 6
# hu calendar 2020
# hu calendar 202101,2,3,4,5
# hu calendar 6,7,8
```

文件树

```shell
hu tree

# output:
├─bin
│ └─hu.exe
├─external
├─method
├─class.json
├─command.json
├─converter.json
├─external.conf
└─hutool.jar
```

计算倒计时

```shell
hu countdown 123456789 ms
# 时间单位可以参考最下方的对照表

# output:
1天10小时17分36秒789毫秒
```

## ZIP包安装

所需环境

- git
- java 8+

下载本项目

```shell
git clone https://gitee.com/code4everything/hutool-cli.git
```

下载对应的 [ZIP包](http://share.qiniu.easepan.xyz/tool/hutool/hu-1.3.zip) ，目录结构如下

```text
├─bin
│ ├─hu
│ ├─hu-mac
│ └─hu.exe
└─hutool.jar
```

下载完成后解压ZIP包，并将 hutool.jar 和 bin目录中与平台对应的可执行文件移动到 hutool-cli/hutool 目录下，如windows对应的`hu.exe`文件，linux对应的`hu`
文件，macos对应的`hu-mac`文件（Mac移动后需重命名为`hu`），移动后目录结构如下。

## 源码安装

> 也可用于更新 `hutool.jar` 文件，当已有 `bin` 目录文件，更新版本时无需再下载上面的 `zip` 包。

先拉取最新代码：`git pull`，然后执行打包命令，通常情况我们只需要执行PACK打包命令即可。

- `gradle pack -x test` 打包可执行JAR包，需要JAVA环境。
- `gradle install -x test` 执行上面的PACK打包命令，并构建对应平台的二进制执行文件（即`hu(.exe)`），需要GO语言环境。

> `-x test` 表示跳过测试用例。

## 配置环境变量

新建 `HUTOOL_PATH` 变量，对应的路径如下：`your_path/hutool-cli/hutool`，目录结构如下：

```text
├─bin
│ └─hu(.exe)
├─method
├─class.json
├─command.json
└─hutool.jar
```

最后将 `HUTOOL_PATH`/bin 添加到系统变量 `PATH` 中。

## 如何使用

查看支持的指令

```shell
hu

# output:
Usage: hutool-cli [options]
  Options:
    -r, --run, --command
      命令模式，命令（类方法别名）可以精确的定位到静态方法，-r指令可缺省
    -c, --class
      类名称（自动加前缀：cn.hutool.）
    -m, --method
      方法名
    -t, --type
      参数类型（非必须，缺失时可能导致无法精确的定位方法）
    -p, --param, --parameter
      方法需要的参数
    -y, --yank, --copy
      是否将结果复制到剪贴板
    -a, --auto-param
      将剪贴板字符串内容注入指定索引位置的参数
    -v, --version
      查看当前版本
    -d, --debug
      是否开启调试模式
```

查看版本

```shell
hu -v
# output: hutool-cli: v1.2
```

执行一个方法

```shell
hu -c cn.hutool.core.util.IdUtil -m randomUUID
# output: 3214683f-55c1-412e-8b7a-454c57468d99

hu -r cn.hutool.core.codec.Base64#encode -t java.lang.CharSequence -p hutool-cli
# output: aHV0b29sLWNsaQ==
```

通过别名执行

```shell
hu -c base64 -m encode -p 'sky is blue'
# output: c2t5IGlzIGJsdWU=

hu -r encode64 'sky is blue' -y
# output: c2t5IGlzIGJsdWU=
# 说明：-y 表示将输出结果复制到剪贴板

hu -r decode64 -a:0
# output: sky is blue
# 说明：-a:0 表示将剪贴板字符串内容注入到索引位置是0的参数中

# v1.2新功能：支持连续执行，符号 // 分隔多个命令，\\0表示将第一个执行结果作为参数注入，\\1表示将第二个执行结果作为参数注入，依次类推
hu encode64 'test multi cmd' // decode64 \\0
# output: test multi cmd
```

> 在 `-r` 模式下，别名后可直接跟方法需要的参数，当然使用 `-p` 也是支持的，并且 -r 是可以省略的，如最上面生成随机UUID的例子。

执行JavaScript脚本

```shell
hu eval 5+6+3+22+9999
# output: 10035
```

## 查看类有哪些可执行静态方法

```shell
hu methods regex

# regex 是类 cn.hutool.core.util.ReUtil 的别名
# output:
contains(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
contains(regex:java.lang.String, content:java.lang.CharSequence)
count(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
count(regex:java.lang.String, content:java.lang.CharSequence)
delAll(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
delAll(regex:java.lang.String, content:java.lang.CharSequence)
delFirst(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
delFirst(regex:java.lang.String, content:java.lang.CharSequence)
delPre(regex:java.lang.String, content:java.lang.CharSequence)
escape(c:char)
escape(current:java.lang.CharSequence)
extractMulti(group:java.util.regex.Pattern, pattern:java.lang.CharSequence, content:java.lang.String)
extractMulti(regex:java.lang.String, content:java.lang.CharSequence, template:java.lang.String)
extractMultiAndDelPre(group:java.util.regex.Pattern, var:cn.hutool.core.lang.Holder, pattern:java.lang.String)
extractMultiAndDelPre(regex:java.lang.String, contentHolder:cn.hutool.core.lang.Holder, template:java.lang.String)
findAll(pattern:java.util.regex.Pattern, content:java.lang.CharSequence, group:int)
findAll(pattern:java.util.regex.Pattern, content:java.lang.CharSequence, group:int, collection:java.util.Collection)
findAll(regex:java.lang.String, content:java.lang.CharSequence, group:int)
findAll(regex:java.lang.String, content:java.lang.CharSequence, group:int, collection:java.util.Collection)
findAllGroup0(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
findAllGroup0(regex:java.lang.String, content:java.lang.CharSequence)
findAllGroup1(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
findAllGroup1(regex:java.lang.String, content:java.lang.CharSequence)
get(pattern:java.util.regex.Pattern, content:java.lang.CharSequence, groupIndex:int)
get(regex:java.lang.String, content:java.lang.CharSequence, groupIndex:int)
getAllGroups(i:java.util.regex.Pattern, startGroup:java.lang.CharSequence, groupCount:boolean)
getAllGroups(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
getFirstNumber(StringWithNumber:java.lang.CharSequence)
getGroup0(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
getGroup0(regex:java.lang.String, content:java.lang.CharSequence)
getGroup1(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
getGroup1(regex:java.lang.String, content:java.lang.CharSequence)
isMatch(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
isMatch(regex:java.lang.String, content:java.lang.CharSequence)
replaceAll(content:java.lang.CharSequence, regex:java.lang.String, replacementTemplate:java.lang.String)
replaceAll(e:java.lang.CharSequence, str:java.util.regex.Pattern, pattern:cn.hutool.core.lang.func.Func1)
replaceAll(group:java.lang.CharSequence, var:java.util.regex.Pattern, replacement:java.lang.String)
replaceAll(str:java.lang.CharSequence, regex:java.lang.String, replaceFun:cn.hutool.core.lang.func.Func1)
```

> v1.2版本已支持输出方法形参名称

## 别名查看

查看有哪些类方法别名（可以精确的定位到方法）

```shell
hu -r alias

# 或者 hutool alias
# output:
assignable  = org.code4everything.hutool.Utils#assignableFrom(sourceClass:java.lang.Class, testClass:java.lang.Class)
between     = cn.hutool.core.date.DateUtil#between(beginDate:java.util.Date, endDate:java.util.Date, unit:cn.hutool.core.date.DateUnit=day)
calc        = org.code4everything.hutool.Utils#calc(expression:java.lang.String, scale:int=0)
class       = org.code4everything.hutool.Utils#parseClassName(className:java.lang.String)
cpuinfo     = cn.hutool.system.oshi.OshiUtil#getCpuInfo()
creditc     = cn.hutool.core.util.CreditCodeUtil#randomCreditCode()
date2ms     = org.code4everything.hutool.Utils#date2Millis(date:java.util.Date)
decode32    = cn.hutool.core.codec.Base32#decodeStr(source:java.lang.String)
decode64    = cn.hutool.core.codec.Base64#decodeStr(source:java.lang.CharSequence)
decodeqr    = cn.hutool.extra.qrcode.QrCodeUtil#decode(qrCodeFile:java.io.File)
download    = cn.hutool.http.HttpUtil#downloadFileFromUrl(url:java.lang.String, destFile:java.io.File)
encode32    = cn.hutool.core.codec.Base32#encode(source:java.lang.String)
encode64    = cn.hutool.core.codec.Base64#encode(source:java.lang.CharSequence)
encode64url = cn.hutool.core.codec.Base64#encodeUrlSafe(source:java.lang.CharSequence)
eval        = cn.hutool.script.ScriptUtil#eval(e:java.lang.String)
fconvert    = cn.hutool.core.util.CharsetUtil#convert(file:java.io.File, srcCharset:java.nio.charset.Charset, destCharset:java.nio.charset.Charset)
fopen       = cn.hutool.core.swing.DesktopUtil#open(e:java.io.File=.)
fr          = cn.hutool.core.io.FileUtil#readUtf8String(file:java.io.File)
ftype       = cn.hutool.core.io.FileUtil#getType(file:java.io.File)
fw          = cn.hutool.core.io.FileUtil#writeUtf8String(content:java.lang.String, file:java.io.File)
get         = cn.hutool.http.HttpUtil#get(urlString:java.lang.String)
grep        = org.code4everything.hutool.Utils#grep(line:java.util.regex.Pattern, pattern:java.util.List)
hex         = cn.hutool.core.util.HexUtil#encodeHexStr(data:java.lang.String)
idcard      = cn.hutool.core.util.IdcardUtil#getIdcardInfo(idcard:java.lang.String)
jinfo       = cn.hutool.system.SystemUtil#props()
lower       = org.code4everything.hutool.Utils#toLowerCase(str:java.lang.String)
lunar       = org.code4everything.hutool.Utils#lunar(date:java.util.Date)
match       = cn.hutool.core.util.ReUtil#isMatch(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
md5         = cn.hutool.crypto.SecureUtil#md5(data:java.lang.String)
methods     = org.code4everything.hutool.Utils#outputPublicStaticMethods(className:java.lang.String)
mkdir       = cn.hutool.core.io.FileUtil#mkdir(dir:java.io.File)
ms2date     = cn.hutool.core.date.DateUtil#date(date:long)
now         = cn.hutool.core.date.DateUtil#date()
objectid    = cn.hutool.core.util.IdUtil#objectId()
pinyin      = cn.hutool.extra.pinyin.PinyinUtil#getPinyin(str:java.lang.String)
post        = cn.hutool.http.HttpUtil#post(urlString:java.lang.String, body:java.lang.String={})
qrcode      = cn.hutool.extra.qrcode.QrCodeUtil#generate(content:java.lang.String, width:int=1000, height:int=1000, targetFile:java.io.File)
random      = cn.hutool.core.util.RandomUtil#randomLong(min:long=0, max:long=9223372036854775807)
randomc     = cn.hutool.core.img.ImgUtil#randomColor()
randomd     = cn.hutool.core.util.RandomUtil#randomDouble(min:double=0, max:double=9223372036854775807)
randompass  = cn.hutool.core.util.RandomUtil#randomString(number:java.lang.String=abcdefghijkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ234567892345678923456789, i:int=16)
randoms     = cn.hutool.core.util.RandomUtil#randomString(length:int=16)
regex       = cn.hutool.core.util.ReUtil#isMatch(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
second      = cn.hutool.core.date.DateUtil#currentSeconds()
sha1        = cn.hutool.crypto.SecureUtil#sha1(data:java.lang.String)
sha256      = cn.hutool.crypto.SecureUtil#sha256(data:java.lang.String)
split       = cn.hutool.core.text.CharSequenceUtil#splitTrim(str:java.lang.CharSequence, separator:java.lang.CharSequence=,)
str2unicode = cn.hutool.core.text.UnicodeUtil#toUnicode(str:java.lang.String)
suppers     = org.code4everything.hutool.Utils#getSupperClass(clazz:java.lang.Class)
sysinfo     = cn.hutool.system.oshi.OshiUtil#getSystem()
test        = cn.hutool.core.util.ReUtil#isMatch(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
unicode2str = cn.hutool.core.text.UnicodeUtil#toString(c:java.lang.String)
upper       = org.code4everything.hutool.Utils#toUpperCase(str:java.lang.String)
uuid        = cn.hutool.core.util.IdUtil#randomUUID()
uuid0       = cn.hutool.core.util.IdUtil#simpleUUID()
week        = cn.hutool.core.date.DateUtil#dayOfWeekEnum(date:java.util.Date)
weekend     = cn.hutool.core.date.DateUtil#endOfWeek(date:java.util.Date)
```

查看类名称别名

```shell
hu -c alias

# output:
base32      = cn.hutool.core.codec.Base32
base64      = cn.hutool.core.codec.Base64
caesar      = cn.hutool.core.codec.Caesar
charset     = cn.hutool.core.util.CharsetUtil
clipboard   = cn.hutool.core.swing.clipboard.ClipboardUtil
credit      = cn.hutool.core.util.CreditCodeUtil
csu         = cn.hutool.core.text.CharSequenceUtil
date        = cn.hutool.core.date.DateUtil
desensitize = cn.hutool.core.util.DesensitizedUtil
emoji       = cn.hutool.extra.emoji.EmojiUtil
file        = cn.hutool.core.io.FileUtil
hash        = cn.hutool.core.util.HashUtil
hex         = cn.hutool.core.util.HexUtil
http        = cn.hutool.http.HttpUtil
id          = cn.hutool.core.util.IdUtil
img         = cn.hutool.core.img.ImgUtil
list        = cn.hutool.core.collection.ListUtil
math        = cn.hutool.core.math.MathUtil
net         = cn.hutool.core.net.NetUtil
p           = cn.hutool.core.swing.DesktopUtil
phone       = cn.hutool.core.util.PhoneUtil
pinyin      = cn.hutool.extra.pinyin.PinyinUtil
qrcode      = cn.hutool.extra.qrcode.QrCodeUtil
random      = cn.hutool.core.util.RandomUtil
reflect     = cn.hutool.core.util.ReflectUtil
regex       = cn.hutool.core.util.ReUtil
robot       = cn.hutool.core.swing.RobotUtil
screen      = cn.hutool.core.swing.ScreenUtil
script      = cn.hutool.script.ScriptUtil
secure      = cn.hutool.crypto.SecureUtil
str         = cn.hutool.core.util.StrUtil
system      = cn.hutool.system.SystemUtil
unicode     = cn.hutool.core.text.UnicodeUtil
zip         = cn.hutool.core.util.ZipUtil
```

查看类别名下方法别名，有以下两种方式：

```shell
hu -c base64 -m alias

# output:
decode    = decodeStr(source:java.lang.CharSequence)
encode    = encode(source:java.lang.CharSequence)
encodeurl = encodeUrlSafe(source:java.lang.CharSequence)
```

```shell
hu -r date#alias

# output:
between = between(beginDate:java.util.Date, endDate:java.util.Date, unit:cn.hutool.core.date.DateUnit=day)
ms2date = date(date:java.util.Date)
now     = now()
second  = currentSeconds()
week    = dayOfWeekEnum(date:java.util.Date)
weekend = endOfWeek(date:java.util.Date)
```

> 我们可以通过关键字 `alias` 来查看命令、类名、方法名已有的别名

## 覆盖别名方法

使用别名文件定义的类名和方法名，参数类型及数量替换为终端命令中使用特定语法输入的内容。

举个例子，比如别名文件中定义的`md5`方法是：`cn.hutool.crypto.SecureUtil#md5(data:java.lang.String)`，传入的是一个`string`类型的参数，但是现在我们想要计算一个文件的MD5。

如何做呢？方法一，再定义一个计算文件MD5的别名，缺点导致别名过多，不方便记忆。

方法二，覆盖别名，语法：`hu command@type1,type2 param`。

还是上面的`md5`，首先查看它的重载方法有哪些？

```shell
hu md5@

# output:
md5()
md5(data:java.io.InputStream)
md5(data:java.lang.String)
md5(dataFile:java.io.File)
```

我们可以看到该方法是支持传入文件的，这时我们直接使用覆盖别名即可：

```shell
# file 是 java.io.File 的别名
hu md5@file command.json

# output:
268f6d715cf4ec191a96b80c29f2449f
```

> `hu command@` 查看别名的重载方法。

## 默认值

我们可以在别名文件中定义参数默认值，执行方法时默认值要么都缺省，要么都填上。

举个例子，如生成二维码图片：

查看生成二维码的命令

```shell
# 查找qrcode相关命令
hu alias // grep qr \\0

# output:
decodeqr    = cn.hutool.extra.qrcode.QrCodeUtil#decode(qrCodeFile:java.io.File)
qrcode      = cn.hutool.extra.qrcode.QrCodeUtil#generate(content:java.lang.String, width:int=1000, height:int=1000, targetFile:java.io.File)
randompass    = cn.hutool.core.util.RandomUtil#randomString(number:java.lang.String=abcdefghijkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ234567892345678923456789, i:int=16)
```

然后执行

```shell
# 该方法有默认值，默认值可全缺省
hu qrcode 'qrcode test' /home/test.png
```

或者不使用默认值

```shell
# 虽然该方法有默认值，但我们可以不使用
hu qrcode 'qrcode test' 600 600 /home/test.png
```

## 自定义别名

别名可以用来快速的指向一个类或一个静态方法，格式大致如下，json中的key将作为别名，value包括了别名对应的类名、方法名以及方法所需的参数类型。

```json
{
    "encode64url": {
        "method": "cn.hutool.core.codec.Base64#encodeUrlSafe",
        "paramTypes": [
            "j.char.seq"
        ]
    },
    "echo": {
        /*@符号表示总是解析参数默认值*/
        "method": "qe#run(@string=return args,@boolean=false)"
    }
}
```

> v1.2支持简写部分Java常用类名，滚动至底部查看更多。

hutool-cli 提供了很多常用的别名，参考下面文件：

- 类方法别名参考 [command.json](/hutool/command.json)

- 类名称别名参考 [class.json](/hutool/class.json)

- 方法名称别名参考 [base64-util.json](/hutool/method/base64-util.json)

自定义你的别名后，你还可以 pr 到本仓库哦，让更多人享受到 hutool 带来的便捷吧。

> 注意：类别名的定义不能超过16个字符。

> v1.2支持定义私有别名啦，定义路径 `{user.home}/hutool-cli/`，文件名和别名格式参照上面说明，程序会优先读取用户定义的私有别名。

## 加载外部类

方法一，在 `HUTOOL_PATH` 对应的目录下新建external目录，将类class类文件（包含包名目录）拷贝到external文件夹中即可，如类 `com.example.Test`
对应的路径 `external/com/example/Test.class`。

方法二，推荐，在 `HUTOOL_PATH`
对应的目录下新建external.conf文件，在文件中定义类加载路径（不包含包路径），多个用英文逗号分隔，还是用上面的类举例，假设类绝对路径是 `/home/java/com/examaple/Test.class`
，那么文件定义路径 `/home/java` 即可。

external.conf文件支持mvn坐标，但前提是本地maven仓库已有对应的jar包，比如：

```txt
// 这是注释
/path/folder
/path/test.jar
mvn:org.code4everything:wetool-plugin-support:1.6.0
```

> 你可以基于此功能开发适用于本地的指令。

## 最后

如果你觉得项目还不错，记得Star哟，欢迎 pr。

## 常用类名简写对照表

|简写名|类全名|
|---|---|
|string|java.lang.String|
|j.char.seq|java.lang.CharSequence|
|file|java.io.File|
|charset|java.nio.charset.Charset|
|date|java.util.Date|
|class|java.lang.Class|
|j.boolean|java.lang.Boolean|
|j.byte|java.lang.Byte|
|j.short|java.lang.Short|
|j.integer|java.lang.Integer|
|j.char|java.lang.Character|
|j.long|java.lang.Long|
|j.float|java.lang.Float|
|j.double|java.lang.Double|
|reg.pattern|java.util.regex.Pattern|
|map|java.util.Map|
|list|java.util.List|
|set|java.util.Set|

> 查看简写是否正确，可通过查看命名 `hu class` 返回的结果是否是一个合法Java类名来判断，如 `hu class j.float` 返回 `java.lang.Float`，`hu class no.class` 返回 `no.class` 就不是一个合法的类名，此方法也可用来判断类别名哦。

## 参数格式说明

#### 日期格式

转换器支持对日期参数进行偏移计算，如把当前日期挪后5天：`hu date now+5d` 或 `hu date now+7-2d`，第一个加号表示需要进行偏移计算，后面紧跟偏移量的计算公式，以及偏移单位。

同时我们还可以对日期进行begin和end计算，比如今天的开始：`hu date now<d`，本周的结束时间：`hu date now>w`，符号 `<` 表示begin运算，`>` 表示end运算，其后紧跟偏移单位。

日期自动补全机制，如本月6号输入 `hu date 06` 即可，本年3月13号输入 `hu date 03-13` 即可，简化输入。

日期简写表

|简写名|说明|
|---|---|
|now|当前时间|
|today|今天的开始时间|
|yesterday|昨天的开始时间|
|tomorrow|明天的开始时间|

偏移单位对照表

|单位名称|说明|
|---|---|
|ms|毫秒|
|s|秒|
|sec|秒|
|min|分钟|
|h|小时|
|hour|小时|
|d|天|
|day|天|
|w|周|
|week|周|
|m|月|
|mon|月|
|month|月|
|y|年|
|year|年|
