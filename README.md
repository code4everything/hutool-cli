# hutool-cli

### 简介

顾名思义，这是一个可以在终端执行的 hutool。

那 hutool-cli 可以做什么呢？

首先Hutool是一个非常好用的Java工具库，提供了非常多地静态工具方法供大家使用，极大地提高了我们日常的开发效率；
但是如果我们想要快速的知道一个方法的运行效果，或者是调试一些方法，亦或者是想利用一些工具方法生成一些内容并复制它到其他应用程序，那我们就不得不写个测试类，然后再调一下对应的方法，最后查看执行结果，但这未免显得过于费劲。

于是 hutool-cli 把 hutool 带到了终端中，让我们可以直接通过命令去执行一个静态的工具方法，让日常的开发变得更有效率。

比如我们想生成一个随机UUID，现在只需要打开终端执行下面命令：

```shell
hutool random-uuid
# output: 483cc7fc-4b22-4188-8f1c-dc1ce4b6d3ee
```

### ZIP包安装

所需环境

- git
- java8 above

下载本项目

```shell
git clone https://gitee.com/code4everything/hutool-cli.git
```

下载对应的ZIP包

- [windows](http://share.qiniu.easepan.xyz/tool/hutool/windows-1.1.zip)
- [linux](http://share.qiniu.easepan.xyz/tool/hutool/linux-1.1.zip)
- [macos](http://share.qiniu.easepan.xyz/tool/hutool/darwin-1.1.zip)

下载完成后解压ZIP包，并将 hutool.jar 和 bin目录移动到 hutool-cli/hutool 目录下

> 说明：[WeTool工具](https://gitee.com/code4everything/wetool) 支持无 `hutool` 前缀执行命令。

### 配置环境变量

新建 `HUTOOL_PATH` 变量，对应的路径如下：`your_path/hutool-cli/hutool`，目录结构如下：

```text
├─bin
│  └─hutool(.exe)
├─method
├─class.json
├─command.json
└─hutool.jar
```

最后将 `HUTOOL_PATH`/bin 添加到系统变量 `PATH` 中。

### 如何使用

查看支持的参数

```shell
hutool

# output:
Usage: hutool-cli [options]
  Options:
    -r, --run, --command
      命令模式，命令可以精确的定位到静态方法，-r指令可缺省
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
hutool -v
# output: hutool-cli: v1.2
```

执行一个方法

```shell
hutool -c cn.hutool.core.util.IdUtil -m randomUUID
# output: 3214683f-55c1-412e-8b7a-454c57468d99

hutool -r core.codec.Base64#encode -t java.lang.CharSequence -p hutool-cli
# output: aHV0b29sLWNsaQ==
# 说明：类名自动补前缀'cn.hutool.'，并且命令模式支持组合类名和方法名称
```

通过别名执行

```shell
hutool -c base64 -m encode -p 'sky is blue'
# output: c2t5IGlzIGJsdWU=

hutool -r base64-encode 'sky is blue' -y
# output: c2t5IGlzIGJsdWU=
# 说明：-y 表示将输出结果复制到剪贴板

hutool -r base64-decode -a:0
# output: sky is blue
# 说明：-a:0 表示将剪贴板字符串内容注入到索引位置是0的参数中
```

> 在 `-r` 模式下，别名后可直接跟方法需要的参数，当然使用 `-p` 也是支持的，并且 -r 是可以省略的，如最上面生成随机UUID的例子。

执行JavaScript脚本

```shell
hutool eval 5+6+3+22+9999
# output: 10035
```

### 查看类有哪些可执行静态方法

```shell
hutool methods regex

# regex 是类 cn.hutool.core.util.ReUtil 的别名
# output:
replaceAll(group:java.lang.CharSequence, var:java.util.regex.Pattern, replacement:java.lang.String)
count(regex:java.lang.String, content:java.lang.CharSequence)
replaceAll(e:java.lang.CharSequence, str:java.util.regex.Pattern, pattern:cn.hutool.core.lang.func.Func1)
findAll(regex:java.lang.String, content:java.lang.CharSequence, group:int)
getGroup0(regex:java.lang.String, content:java.lang.CharSequence)
getGroup1(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
delFirst(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
getAllGroups(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
findAllGroup1(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
getFirstNumber(StringWithNumber:java.lang.CharSequence)
isMatch(regex:java.lang.String, content:java.lang.CharSequence)
findAll(regex:java.lang.String, content:java.lang.CharSequence, group:int, collection:java.util.Collection)
getGroup0(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
findAll(pattern:java.util.regex.Pattern, content:java.lang.CharSequence, group:int, collection:java.util.Collection)
findAllGroup0(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
extractMulti(regex:java.lang.String, content:java.lang.CharSequence, template:java.lang.String)
isMatch(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
getGroup1(regex:java.lang.String, content:java.lang.CharSequence)
escape(current:java.lang.CharSequence)
delAll(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
contains(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
count(pattern:java.util.regex.Pattern, content:java.lang.CharSequence)
delFirst(regex:java.lang.String, content:java.lang.CharSequence)
replaceAll(str:java.lang.CharSequence, regex:java.lang.String, replaceFun:cn.hutool.core.lang.func.Func1)
findAllGroup1(regex:java.lang.String, content:java.lang.CharSequence)
get(pattern:java.util.regex.Pattern, content:java.lang.CharSequence, groupIndex:int)
extractMulti(group:java.util.regex.Pattern, pattern:java.lang.CharSequence, content:java.lang.String)
escape(c:char)
delPre(regex:java.lang.String, content:java.lang.CharSequence)
getAllGroups(i:java.util.regex.Pattern, startGroup:java.lang.CharSequence, groupCount:boolean)
get(regex:java.lang.String, content:java.lang.CharSequence, groupIndex:int)
findAll(pattern:java.util.regex.Pattern, content:java.lang.CharSequence, group:int)
findAllGroup0(regex:java.lang.String, content:java.lang.CharSequence)
contains(regex:java.lang.String, content:java.lang.CharSequence)
replaceAll(content:java.lang.CharSequence, regex:java.lang.String, replacementTemplate:java.lang.String)
extractMultiAndDelPre(regex:java.lang.String, contentHolder:cn.hutool.core.lang.Holder, template:java.lang.String)
extractMultiAndDelPre(group:java.util.regex.Pattern, var:cn.hutool.core.lang.Holder, pattern:java.lang.String)
delAll(regex:java.lang.String, content:java.lang.CharSequence)
```

> v1.2版本已支持输出方法形参名称

### 别名查看

查看有哪些命令别名（命令可以精确定位到方法）

```shell
hutool -r alias

# 或者 hutool alias
# output:
2hex               = cn.hutool.core.util.HexUtil#encodeHexStr(data:java.lang.String)
base32-decode      = cn.hutool.core.codec.Base32#decodeStr(source:java.lang.String)
base32-encode      = cn.hutool.core.codec.Base32#encode(source:java.lang.String)
base64-decode      = cn.hutool.core.codec.Base64#decodeStr(source:java.lang.CharSequence)
base64-encode      = cn.hutool.core.codec.Base64#encode(source:java.lang.CharSequence)
base64-encode-url  = cn.hutool.core.codec.Base64#encodeUrlSafe(source:java.lang.CharSequence)
convert-charset    = cn.hutool.core.util.CharsetUtil#convert(file:java.io.File, srcCharset:java.nio.charset.Charset, destCharset:java.nio.charset.Charset)
cpu-info           = cn.hutool.system.oshi.OshiUtil#getCpuInfo()
curr-sec           = cn.hutool.core.date.DateUtil#currentSeconds()
date-between       = cn.hutool.core.date.DateUtil#between(beginDate:java.util.Date, endDate:java.util.Date, unit:cn.hutool.core.date.DateUnit=day)
date2ms            = org.code4everything.hutool.Utils#date2Millis(date:java.util.Date)
eval               = cn.hutool.script.ScriptUtil#eval(e:java.lang.String)
file-read          = cn.hutool.core.io.FileUtil#readUtf8String(path:java.lang.String)
file-type          = cn.hutool.core.io.FileUtil#getType(file:java.io.File)
get                = cn.hutool.http.HttpUtil#get(urlString:java.lang.String)
hardware-info      = cn.hutool.system.oshi.OshiUtil#getHardware()
idcard             = cn.hutool.core.util.IdcardUtil#getIdcardInfo(idcard:java.lang.String)
java-info          = cn.hutool.system.SystemUtil#props()
lower              = org.code4everything.hutool.Utils#toLowerCase(str:java.lang.String)
lunar              = org.code4everything.hutool.Utils#lunar(date:java.util.Date)
md5                = cn.hutool.crypto.SecureUtil#md5(data:java.lang.String)
methods            = org.code4everything.hutool.Utils#outputPublicStaticMethods(modifiers:java.lang.Class)
mkdir              = cn.hutool.core.io.FileUtil#mkdir(dir:java.io.File)
ms2date            = cn.hutool.core.date.DateUtil#date(date:java.util.Date)
now                = cn.hutool.core.date.DateUtil#date()
object-id          = cn.hutool.core.util.IdUtil#objectId()
pinyin             = cn.hutool.extra.pinyin.PinyinUtil#getPinyin(str:java.lang.String)
post               = cn.hutool.http.HttpUtil#post(urlString:java.lang.String, body:java.lang.String)
processor-info     = cn.hutool.system.oshi.OshiUtil#getProcessor()
qrcode-decode      = cn.hutool.extra.qrcode.QrCodeUtil#decode(qrCodeFile:java.io.File)
qrcode-generate    = cn.hutool.extra.qrcode.QrCodeUtil#generate(content:java.lang.String, width:int=1000, height:int=1000, targetFile:java.io.File)
random             = cn.hutool.core.util.RandomUtil#randomLong()
random-color       = cn.hutool.core.img.ImgUtil#randomColor()
random-credit-code = cn.hutool.core.util.CreditCodeUtil#randomCreditCode()
random-double      = cn.hutool.core.util.RandomUtil#randomDouble()
random-str         = cn.hutool.core.util.RandomUtil#randomString(length:int)
random-uuid        = cn.hutool.core.util.IdUtil#randomUUID()
regex-match        = cn.hutool.core.util.ReUtil#isMatch(regex:java.lang.String, content:java.lang.CharSequence)
sha1               = cn.hutool.crypto.SecureUtil#sha1(data:java.lang.String)
sha256             = cn.hutool.crypto.SecureUtil#sha256(data:java.lang.String)
simple-uuid        = cn.hutool.core.util.IdUtil#simpleUUID()
split              = cn.hutool.core.text.CharSequenceUtil#splitTrim(str:java.lang.CharSequence, separator:java.lang.CharSequence)
str2unicode        = cn.hutool.core.text.UnicodeUtil#toUnicode(str:java.lang.String)
system-info        = cn.hutool.system.oshi.OshiUtil#getSystem()
test               = cn.hutool.core.util.ReUtil#isMatch(regex:java.lang.String, content:java.lang.CharSequence)
unicode2str        = cn.hutool.core.text.UnicodeUtil#toString(c:java.lang.String)
upper              = org.code4everything.hutool.Utils#toUpperCase(str:java.lang.String)
week               = cn.hutool.core.date.DateUtil#dayOfWeekEnum(date:java.util.Date)
week-end           = cn.hutool.core.date.DateUtil#endOfWeek(date:java.util.Date)
```

查看类名称别名

```shell
hutool -c alias

# output:
base32  = cn.hutool.core.codec.Base32
base64  = cn.hutool.core.codec.Base64
caesar  = cn.hutool.core.codec.Caesar
charset = cn.hutool.core.util.CharsetUtil
date    = cn.hutool.core.date.DateUtil
emoji   = cn.hutool.extra.emoji.EmojiUtil
file    = cn.hutool.core.io.FileUtil
hash    = cn.hutool.core.util.HashUtil
hex     = cn.hutool.core.util.HexUtil
http    = cn.hutool.http.HttpUtil
id      = cn.hutool.core.util.IdUtil
list    = cn.hutool.core.collection.ListUtil
math    = cn.hutool.core.math.MathUtil
net     = cn.hutool.core.net.NetUtil
phone   = cn.hutool.core.util.PhoneUtil
pinyin  = cn.hutool.extra.pinyin.PinyinUtil
qrcode  = cn.hutool.extra.qrcode.QrCodeUtil
random  = cn.hutool.core.util.RandomUtil
reflect = cn.hutool.core.util.ReflectUtil
regex   = cn.hutool.core.util.ReUtil
script  = cn.hutool.script.ScriptUtil
secure  = cn.hutool.crypto.SecureUtil
str     = cn.hutool.core.util.StrUtil
system  = cn.hutool.system.SystemUtil
unicode = cn.hutool.core.text.UnicodeUtil
```

查看类别名下方法别名，有以下两种方式：

```shell
hutool -c base64 -m alias

# output:
decode     = decodeStr(source:java.lang.CharSequence)
encode     = encode(source:java.lang.CharSequence)
encode-url = encodeUrlSafe(source:java.lang.CharSequence)
```

```shell
hutool -r date#alias

# output:
between     = between(beginDate:java.util.Date, endDate:java.util.Date, unit:cn.hutool.core.date.DateUnit=day)
curr-sec    = currentSeconds()
ms2datetime = date(date:java.util.Date)
now         = now()
week        = dayOfWeekEnum(date:java.util.Date)
week-end    = endOfWeek(date:java.util.Date)
```

> 我们可以通过关键字 `alias` 来查看命令、类名、方法名已有的别名

### 默认值

我们可以在别名文件中定义参数默认值，执行方法时默认值要么都缺省，要么都填上。

举个例子，如生成二维码图片：

查看生成二维码的命令

```shell
hutool alias | grep qrcode

# output:

qrcode-decode      = cn.hutool.extra.qrcode.QrCodeUtil#decode(java.io.File)
qrcode-generate    = cn.hutool.extra.qrcode.QrCodeUtil#generate(java.lang.String, java.lang.Integer=1000, java.lang.Integer=1000, java.io.File)
```

然后执行

```shell
# 该方法有默认值，默认值可全缺省
hutool qrcode-generate 'qrcode test' /home/test.png
```

或者不使用默认值

```shell
# 虽然该方法有默认值，但我们可以不使用
hutool qrcode-generate 'qrcode test' 600 600 /home/test.png
```

### 自定义别名

别名可以用来快速的指向一个类或一个静态方法，格式大致如下，json中的key将作为别名，value包括了别名对应的类名、方法名以及方法所需的参数类型。

```json
{
    "base64-encode-url": {
        "method": "cn.hutool.core.codec.Base64#encodeUrlSafe",
        "paramTypes": [
            "j.char.seq"
        ]
    }
}
```

> v1.2支持简写部分Java常用类名，滚动至底部查看更多。

hutool-cli 提供了很多常用的别名，参考下面文件：

- 命令别名参考 [command.json](/hutool/command.json)

- 类名称别名参考 [class.json](/hutool/class.json)

- 方法名称别名参考 [base64-util.json](/hutool/method/base64-util.json)

自定义你的别名后，你还可以 pr 到本仓库哦，让更多人享受到 hutool 带来的便捷吧。

> v1.2支持定义私有别名啦，定义路径 `{user.home}/hutool-cli/`，文件名和别名格式参照上面说明，程序会优先读取用户定义的私有别名。

### 最后

如果你觉得项目还不错，记得Star哟，欢迎 pr。

### 常用类名简写对照表

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
|regex.pattern|java.util.regex.Pattern|
|map|java.util.Map|
|list|java.util.List|
|set|java.util.Set|

> 查看简写是否正确，可通过查看命名 `hutool class` 返回的结果是否是一个合法Java类名来判断，如 `hutool class j.float` 返回 `java.lang.Float`，`hutool class no.class` 返回 `no.class` 就不是一个合法的类名，此方法也可用来判断类别名哦。
