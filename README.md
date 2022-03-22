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

随着工具不断的迭代更新，现在，已经不再只是终端里的Hutool了，更多功能等待你来探索哦。

## 内置命令

jq命令支持（jq是一款强大的命令行json数据处理工具）

```shell
hu jq '.' '{"tutorial":"https://stedolan.github.io/jq/tutorial/"}'

#output:
{
  "tutorial" : "https://stedolan.github.io/jq/tutorial/"
}

# 其他示例：
hu jq 'to_entries|map(.key+"="+.value.clazz)|.[]' hutool/class.json strip
```

> [JQ基本语法](https://stedolan.github.io/jq/manual/#Basicfilters)

文件查找

```shell
# 命令格式
hu find {dir} {nameFilter} [(file|dir)] [hidden] [ignoreempty] [depth:{n}] [(c|a|u)time+{n}{timeUnit}]

# 举个例子：
hu find . java file depth:9 utime+7d hidden # 查找当前目录下文件名包含java并且最近7天修改过的文件，遍历最大深度为9层目录，hidden表示允许查找隐藏文件
hu find ~ '.*' dir ctime:9d ignoreempty # 查找用户目录下最近9天创建的，且不为空的文件夹
```

> 说明：{}表示必传变量，[]表示可选参数，()表示枚举值，可选参数不区分位置，时间单位参考最后面的日期参数格式。

FigletBanner生成（ASCII艺术字）

```shell
hu figlet hutool-cli
# hu figlet {message} {font-name=standard}

# output:
  _               _                     _                  _   _ 
 | |__    _   _  | |_    ___     ___   | |           ___  | | (_)
 | '_ \  | | | | | __|  / _ \   / _ \  | |  _____   / __| | | | |
 | | | | | |_| | | |_  | (_) | | (_) | | | |_____| | (__  | | | |
 |_| |_|  \__,_|  \__|  \___/   \___/  |_|          \___| |_| |_|
                                                                 

```

> [figlet fonts](http://www.figlet.org/fontdb.cgi) ，下载后放到 `{user.home}/hutool-cli/fonts/figlet/` 目录下即可。

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

下载对应的 [ZIP包](http://share.qiniu.easepan.xyz/tool/hutool/hu-1.5.zip) ，目录结构如下

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
      命令模式，命令（方法别名）可以精确的定位到静态方法，-r指令可缺省
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

hu encode64 'sky is blue' -y
# output: c2t5IGlzIGJsdWU=
# 说明：-y 表示将输出结果复制到剪贴板

hu decode64 -a:0
# output: sky is blue
# 说明：-a:0 表示将剪贴板字符串内容注入到索引位置是0的参数中

# v1.2新功能：支持连续执行，符号 // 分隔多个命令，\\0表示将第一个执行结果作为参数注入，\\1表示将第二个执行结果作为参数注入，依次类推
hu encode64 'test multi cmd' // decode64 \\0
# output: test multi cmd
```

> 在 `-r` 模式下，别名后可直接跟方法需要的参数，当然使用 `-p` 也是支持的，并且 -r 是可以省略的，如最上面生成随机UUID的例子。

## 查看类有哪些可执行静态方法

```shell
hu methods regex

# regex 是类 cn.hutool.core.util.ReUtil 的别名
# output:
contains(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
contains(regex:java.lang.String, content:java.lang.CharSequence)
count(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
count(regex:java.lang.String, content:java.lang.CharSequence)
..........
```

> v1.2版本已支持输出方法形参名称

## 别名查看

查看有哪些方法别名（可以精确的定位到方法）

```shell
# 查看包含random的别名
hu alias random

# output:
random     = cn.hutool.core.util.RandomUtil#randomString(length:int=16)
randomc    = cn.hutool.core.img.ImgUtil#randomColor()
randomi    = cn.hutool.core.util.RandomUtil#randomLong(min:long=0, max:long=9223372036854775807)
randompass = cn.hutool.core.util.RandomUtil#randomString(number:java.lang.String=abcdefghijkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ234567892345678923456789, i:int=16)
uuid       = cn.hutool.core.util.IdUtil#randomUUID()
```

查看类名称别名

```shell
# 查看包含base的类别名
hu -c alias -p base

# output:
base32 = cn.hutool.core.codec.Base32
base64 = cn.hutool.core.codec.Base64
```

> 我们可以通过关键字 `alias` 来查看命令、类名、方法名已有的别名

## 覆盖别名方法

使用别名文件定义的类名和方法名，参数类型及数量替换为用户在终端中输入的内容。

举个例子，比如别名文件中定义的`md5`方法是：`cn.hutool.crypto.SecureUtil#md5(data:java.lang.String)`，传入的是一个`string`类型的参数，但是现在我们想要计算一个文件的MD5。

如何做呢？方法一，再定义一个计算文件MD5的别名，缺点导致别名过多，不方便记忆。

方法二，采用覆盖别名方法，语法：`hu command@type1,type2 params`。

还是上面的`md5`举例，首先查看它有哪些重载方法

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
hu alias qr

# output:
decodeqr = cn.hutool.extra.qrcode.QrCodeUtil#decode(qrCodeFile:java.io.File)
qrcode   = cn.hutool.extra.qrcode.QrCodeUtil#generate(content:java.lang.String, width:int=1000, height:int=1000, targetFile:java.io.File=qrcode.png)
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
        ],
        "outArgs": "--copy"
    },
    "echo": {
        /*@符号表示总是解析参数默认值*/
        "method": "qe#run(@string=return args,@boolean=false)",
        "outConverter": "org.code4everything.hutool.converter.LineSepConverter",
        "inConverters": []
    }
}
```

> v1.2支持简写部分Java常用类名，滚动至底部查看更多。

hutool-cli 提供了很多常用的别名，参考下面文件：

- 方法别名参考 [command.json](/hutool/command.json)

- 类名称别名参考 [class.json](/hutool/class.json)

自定义你的别名后，可以 pr 到本仓库哦，让更多人体验到工具带来的便捷~

> 注意：类别名的定义不能超过16个字符。

> v1.2支持定义私有别名啦（优先级高于系统别名），定义路径 `{user.home}/hutool-cli/`，文件名和别名格式参照上面说明，程序会对私有别名和系统别名进行合并。

> v1.6新增在别名文件中定义参数输入输出转换器 `outConverter` `inConverters`，以及输出参数 `outArgs`。

## 加载外部类

方法一，在 `HUTOOL_PATH` 对应的目录下新建external目录，将类class类文件（包含包名目录）拷贝到external文件夹中即可，如类 `com.example.Test`
对应的路径 `external/com/example/Test.class`。

方法二，在 `HUTOOL_PATH` 对应的目录下新建external.conf文件，在文件中定义类加载路径（不包含包路径），使用英文逗号或换行符分隔，还是用上面的类举例，假设类绝对路径是 `/home/java/com/examaple/Test.class`，那么文件定义路径 `/home/java` 即可。

external.conf文件支持mvn坐标，但前提是本地maven仓库已有对应的jar包，比如：

```txt
// 这是注释
/path/folder
/path/test.jar
mvn:org.code4everything:wetool-plugin-support:1.6.0
```

方法三，通过插件方式，安装插件 `hu plugin install /path/xxx.jar`，执行插件 `hu xxx` 或者 `hu p.xxx`
（当插件名被别名占用时，可使用此方式执行），程序将执行插件包内的 `org.code4everything.hutool.PluginEntry#run()` 方法。

> 你可以基于此功能开发适用于本地的指令。

## 好玩的RUN方法

`hu run` 可以执行一段类Java脚本（Alibaba QLExpress），查看帮助说明 `hu run -h`

```text
example: 'arg0.length()' 'some_string_to_get_length'

param1: the script expression

args: the others params will map to args, and very param will map to indexed arg, like arg0 arg1...
auto injected args: currdir, linesep, filesep, userhome
auto injected methods: cmd(p1), nullto(p1,p2), clipboard(), list(p1,p2..pn), join(p1), run(p1,p2..pn), lower(), upper()
you can use it in your expression

cmd(string): execute a command in terminal
nullto(object,object): if p1 is null return p2, else p1
clipboard(): get a string from clipboard
list(object): variable arguments, return a list
list.join(string): join a list to string, like 'list(1,2,3).join("<")'
string.lower() & string.upper(): transfer string to lower case or upper case
run(string): run hu command in ql script, like 'run("base64","some text here")'

ql script grammar: https://github.com/alibaba/QLExpress
```

处理字符串

`hu run 'clipboard().trim().lower().replace(".","#")'`

在脚本中运行hu命令

```shell
hu run 'run("base64","1234")'
hu run 'run("csv2json","some.csv").raw().getJSONObject(0).getString("key")'
```

## 最后

如果你觉得项目还不错，记得Star哟，欢迎 pr。

## 常用类名简写对照表

|简写名|类全名|
|---|---|
|string|java.lang.String|
|object|java.lang.Object|
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
