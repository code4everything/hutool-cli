# hutool-cli

### 简介

顾名思义，这是一个可以在终端执行的 hutool。

那 hutool-cli 可以做什么呢？

首先Hutool是一个非常好用的Java工具库，提供了非常多地静态工具方法供大家使用，极大地提高了我们日常的开发效率；
但是如果我们想要快速的知道一个方法的运行效果，或者是调试一些方法，亦或者是想利用一些工具方法生成一些内容并复制它到其他应用程序，那我们就不得不写个测试类，然后再调一下对应的方法，最后查看执行结果，但这未免显得过于费劲。

于是 hutool-cli 就将 hutool 打包到了终端中去，让我们可以直接通过命令去执行一个静态的工具方法，让日常的开发变得更有效率。

比如我们想生成一个随机UUID，现在只需要打开终端执行下面命令：

```shell
hutool -r random-uuid
# output: 483cc7fc-4b22-4188-8f1c-dc1ce4b6d3ee
```

### 手动安装

所需环境

- java8
- maven
- python
- go

下载本项目

```shell
git clone https://gitee.com/code4everything/hutool-cli.git
```

构建

```shell
cd bin
python package.py
```

### 配置环境变量

新建 `HUTOOL_PATH` 变量，对应的路径如下：`your_path/hutool-cli/hutool`，目录结构如下：

```text
├─bin
│  └─hutool.exe
├─method
├─class.json
├─command.json
└─hutool.jar
```

最后将 `HUTOOL_PATH`/bin 添加到系统变量 `PATH` 中。

### 如何使用

查看版本

```shell
hutool -v
# output: hutool-cli: v1.0
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

hutool -r base64-decode -a
# output: sky is blue
# 说明：-a 表示将剪贴板内容作为参数输入
```

> 在 `-r` 模式下，别名后可直接跟方法需要的参数，当然使用 `-p` 也是支持的。

查看有哪些别名

```shell
hutool -r alias

# output:
base32-decode     = cn.hutool.core.codec.Base32#decodeStr(java.lang.String)
base32-encode     = cn.hutool.core.codec.Base32#encode(java.lang.String)
base64-decode     = cn.hutool.core.codec.Base64#decodeStr(java.lang.CharSequence)
base64-encode     = cn.hutool.core.codec.Base64#encode(java.lang.CharSequence)
base64-encode-url = cn.hutool.core.codec.Base64#encodeUrlSafe(java.lang.CharSequence)
random-uuid       = cn.hutool.core.util.IdUtil#randomUUID()
```

```shell
hutool -c alias

# output:
base32 = cn.hutool.core.codec.Base32
base64 = cn.hutool.core.codec.Base64
str    = cn.hutool.core.util.StrUtil
```

```shell
hutool -c base64 -m alias

# output:
decode     = decodeStr(java.lang.CharSequence)
encode     = encode(java.lang.CharSequence)
encode-url = encodeUrlSafe(java.lang.CharSequence)
```

```shell
hutool -r date#alias
```

> 我们可以通过关键字 `alias` 来查看命令、类名、方法名已有的别名

支持的参数

```text
Usage: hutool-cli [options]
  Options:
    -r, --run, --command
      命令模式
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
      是否将剪贴板内容作为参数输入
    -v, version
      查看当前版本
    -d, --debug
      是否开启调试模式
```

### 自定义别名

别名可以用来快速的指向一个类或一个静态方法，格式大致如下，key将作为别名，value包括了别名对应的类名、方法名以及方法所需的参数类型。

```json
{
    "base64-encode-url": {
        "method": "cn.hutool.core.codec.Base64#encodeUrlSafe",
        "paramTypes": [
            "java.lang.CharSequence"
        ]
    }
}
```

hutool-cli 已经提供了大量常用的别名，参考下面文件：

- 命令别名参考 [command.json](/hutool/command.json)

- 类名称别名参考 [class.json](/hutool/class.json)

- 方法名称别名参考 [base64-util.json](/hutool/method/base64-util.json)

自定义你的别名后，你还可以 pr 到本仓库哦，让更多人享受 hutool 带来的便捷吧。
