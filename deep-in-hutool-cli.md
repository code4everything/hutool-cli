# 走进HutoolCli

## 前言

工欲善其事，必先利其器。 Hutool就是我们编程利器中的其中一把，提高了我们的工作效率，它提供了许多常用的工具方法供我们使用。

本人也是一个Hutool深度使用者，很多方法可以说都是一个小工具，比如BASE64的编码与解码、计算器、时间戳与日期的转换、随机字符串、二维码的生成与解码，正则表达式匹配等等，而且都是调用一个静态方法即可完成。

于是我有了一个想法，就是通过一个简单的终端命令去执行一个Hutool静态方法，让开发变得更有效率，这样的话就需要命令能够精确的匹配到一个类的静态方法，然后我们通过反射去执行即可，看起来挺简单的，实现起来也不复杂，`hutool-cli` 就这样干起来了。

> 如果你熟悉Hutool，那么使用本工具将很轻松，几乎没有学习成本；如果你不熟悉Hutool，那么本工具可以带你走进Hutool；但是如果你不熟悉Java，那使用本工具可能稍显困难。

## 终端命令参数解析

要实现一个终端命令，首先我们需要一个命令参数解析库，搜了一圈，发现Java的命令解析库还是挺多的，常用的有JCommander、CommonsCli、Args4J等，最后综合对比下来，选择了JCommander，因为它文档结构十分清晰，使用起来也非常简单顺手。

JCommander支持数组解析，我们可以将多个参数解析到同一个数组中，比如：`--arr 1 --arr 2 --arr 3` 解析到 `@Parameter(names = "--arr") List<Integer> arr` 中，这特性可以应用到本工具执行静态方法时所需参数的传入功能。

> [JCommander传送门](https://jcommander.org)

## 执行静态方法

有了终端命令参数解析的支持，那我们就可以开始设计参数了，要精确匹配静态方法，那么命令需要传入类全名、方法名、参数类型、方法参数值，通过传入的信息去匹配静态方法，并通过反射去调用它，最后将结果输出到终端。

用于方法定位的参数设计如下，其中为了简化输入，决定忽略方法名的大小写敏感，方法的参数类型也设计为非必填字段，参数类型为空时会根据具体的方法入参个数进行匹配调用。

```java
@Parameter(names = {"-c", "--class"}, description = CLASS_DESC, help = true, order = 1)
public String className;

@Parameter(names = {"-m", "--method"}, description = "the static method name(ignore case)", help = true, order = 2)
public String methodName;

@Parameter(names = {"-t", "--type"}, description = "the class type of parameter(not required)", order = 3)
public List<String> paramTypes = new ArrayList<>();

@Parameter(names = {"-p", "--param", "--parameter"}, description = PARAM_DESC, order = 4)
public List<String> params = new ArrayList<>();
```

比如将字符串 `123456789` 进行BASE64编码，我们需要调用方法 `cn.hutool.core.codec.Base64#encode('123456789')`，命令示例：`-c cn.hutool.core.codec.Base64 -m encode -t java.lang.CharSequence -p 123456789`。

> 当然如果缺省方法参数类型时，可能导致方法匹配错误，进而致使方法参数类型转换失败，最终导致方法调用失败。

## 参数转换器

虽然现在我们可以精确的定位到一个方法，但是执行它还面临着一个问题，我们通过终端传入的都是一连串的字符，但方法入参的类型可能是File、List、Date等等，调用方法时传入字符串会导致方法直接调用失败，所以我们还需要设计个方法参数转换器。

转换器接口设计如下，接口有两个方法需要实现，一个是将字符串转换为Java类型，一个是将Java类型转换为字符串，用于将调用结果格式化输出。

```java
public interface Converter<T> {

    /**
     * convert string to java type
     */
    T string2Object(String string) throws Exception;

    /**
     * convert java object to string
     */
    String object2String(Object object);
}
```

例子1：文件转换器的实现，从代码中可以看出传入的字符串是文件路径，并且支持相对路径，命令示例：`-c cn.hutool.core.io.FileUtil -m readUtf8String -t java.io.File -p test.txt`。

```java
public class FileConverter implements Converter<File> {

    @Override
    public File string2Object(String string) {
        return FileUtil.isAbsolutePath(string) ? FileUtil.file(string) : Paths.get(Hutool.ARG.workDir, string).toFile();
    }

    @Override
    public String object2String(Object object) {
        return object instanceof File ? ((File) object).getAbsolutePath() : "";
    }
}
```

例子2：日期转换器的实现，同样从代码可以看出，日期除了支持常见的格式，比如：yyyy-MM-dd, yyyy-MM-dd HH:mm:ss 等，还支持三个字符串：now, yesterday, tomorrow。

```java
public class DateConverter implements Converter<Date> {

    @Override
    public Date string2Object(String string) {
        if (StrUtil.equalsIgnoreCase("now", string)) {
            return DateUtil.date();
        }
        if (StrUtil.equalsIgnoreCase("yesterday", string)) {
            return DateUtil.offsetDay(DateUtil.date(), -1);
        }
        if (StrUtil.equalsIgnoreCase("tomorrow", string)) {
            return DateUtil.offsetDay(DateUtil.date(), 1);
        }
        return DateUtil.parse(string);
    }

    @Override
    public String object2String(Object object) {
        return object instanceof Date ? Hutool.getSimpleDateFormat().format(object) : "";
    }
}
```

目前引入的转换器并不多，因为现在用到的方法还很有限，转换器的引入也十分简单，支持动态引入，在`converter.json`文件添加已有转换器即可，无需升级，需要时可随时添加的。

> Java常用类型自动转换，无需引入方法参数转换器，比如：Boolean, Integer, Long, Float, Double等等，这些类型会使用fastjson的TypeUtils#cast方法转换。

## 别名设计

上面的示例命令大家都看见了吧，实在是太长了，如果一个功能需要如此冗长的命令，而且还需要你时刻谨记类名和方法名才能得以执行，我相信是不会有人使用的。

所以有了别名的设计，而别名又有三个类型：

- 类方法别名：该类型别名的定义包括了类全名和方法名，以及方法入参类型，可以精确的匹配到一个静态方法。
- 类别名：其定义指向了一个类全名，如果还需要定义该类下的方法别名，还需要定义方法别名的JSON文件路径。
- 方法别名：包括方法名和入参类型，同时需在类别名文件中定义查找路径。

别名的参数解析设计如下，我们可以通过 `-r` 来执行一个类方法别名，并且它还是可以省略的，如 `-r alias` 和 `alias` 效果是一样的，别名后面还可以直接跟参数，无需 `-p` 标识。

```java
@Parameter(names = {"-r", "--run", "--command"}, description = COMMAND_DESC, variableArity = true, order = 0)
public List<String> command = new ArrayList<>();

@Parameter(description = "for command missing", variableArity = true, hidden = true, order = 11)
public List<String> main = new ArrayList<>();
```

类方法别名格式，方法没有参数时，paramTypes可不填写。

```json
{
    "pinyin": {
        "method": "cn.hutool.extra.pinyin.PinyinUtil#getPinyin",
        "paramTypes": [
            "string"
        ]
    }
}
```

类别名格式，别名system定义指向的类，同时还定义了类中方法别名的查找路径：在相对当前路径的 `./method/system-util.json` 路径。

```json
{
    "str": {
        "clazz": "cn.hutool.core.util.StrUtil"
    },
    "system": {
        "clazz": "cn.hutool.system.SystemUtil",
        "methodAliasPaths": [
            "method",
            "system-util.json"
        ]
    }
}
```

方法别名格式，下面示例了system-util.json文件中的别名，如果方法有参数，需要定义paramTypes数组，参考上面的类方法别名。

```json
{
    "memory": {
        "methodName": "getMemoryMXBean"
    },
    "thread": {
        "methodName": "getThreadMXBean"
    },
    "runtime": {
        "methodName": "getRuntimeMXBean"
    },
    "compilation": {
        "methodName": "getCompilationMXBean"
    },
    "os": {
        "methodName": "getOperatingSystemMXBean"
    }
}
```

到这里，我们就可以通过一个很简单的命令去执行一个方法了，比如：`pinyin 魑魅魍魉` 就可以输出 `魑魅魍魉` 的拼音。

> 上面的 `string` 是一个简写的Java类型，其指向了 `java.lang.String`类， [查看内置的类别名对照表](README.md#常用类名简写对照表)

### 输出方法形参名称

截止到这里，hutool-cli已经可以很方便地执行一个方法了，本工具有一个名叫 `methods` 的类方法别名，可以输出一个类所有的静态方法，但是缺少了一个灵魂，那就是方法形参的名称，你可以想象如果一个方法有三五个相同类型的参数，但是我们现在只能看到参数类型，不知道参数名称，不去看Hutool源代码你根本不知道每个参数是用来干嘛的，所以我们还需要形参名称。

为了能够输出方法的形参名称，这里引入 `javassist` 字节码库，这部分代码也挺简单的，不做过多介绍，输出格式：methodName(paramName:paramType)。

```txt
captureScreen()
captureScreen(outFile:java.io.File)
captureScreen(screenRect:java.awt.Rectangle)
captureScreen(screenRect:java.awt.Rectangle, outFile:java.io.File)
click()
keyClick(keyCode:int[])
keyPressString(str:java.lang.String)
keyPressWithAlt(key:int)
keyPressWithCtrl(key:int)
keyPressWithShift(key:int)
mouseMove(x:int, y:int)
mouseWheel(wheelAmt:int)
rightClick()
setDelay(delayMillis:int)
```

### 可执行的二进制文件

到这里我们还只能通过 `java -jar hutool.jar -r alias` 命令来执行，完整的终端命令仍然很长，现在我们需要把 `java -jar hutool.jar` 这段干掉，则需要一个二进制可执行文件，它就像一个中转站，可把从终端拿到的参数传给我们的Java程序，我选择了用 `go` 语言来完成这项工作，go可以把代码直接编译成机器码。

现在我们可以这样来执行`hu alias`，至此hutool-cli基本功能及其实现已全部介绍完成。

> [Go源代码查看](src/main/go/hutool.go)

### 总结

本文简单介绍了本工具是如何一步一步设计完成的：一个想法 -> 终端命令参数解析 -> 静态方法的执行 -> 参数转换器 -> 别名的设计 -> 输出形参名称 -> 二进制可执行文件。

本工具的实现很简单，涉及到的类也不多，主要工作量在别名的定义，除了必须引入的Hutool库及其扩展包依赖的库以外，还引入了JCommander和Javassist库。

更多细节可以参考源代码。
