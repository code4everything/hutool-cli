# 走进HutoolCli

## 前言

工欲善其事，必先利其器。 Hutool就是我们编程利器中的其中一把，提高了我们的工作效率，它提供了大量常用地工具方法供我们使用。

本人也是一个Hutool深度使用者，每个方法可以说都是一个小工具，比如BASE64的编码与解码、计算器、时间戳与日期的转换、随机字符串、二维码的生成与解码，正则表达式匹配等等，而且都是调用一个静态方法即可完成。

于是我有了一个想法，就是通过一个简单的终端命令去执行一个Hutool静态方法，让开发变得更有效率，如果是这样的话那我们就需要命令能够精确的匹配到一个类的静态方法，然后通过反射去执行即可，看起来挺简单的，开干。

> 如果你熟悉Hutool，那么使用本工具将很会轻松，几乎没有学习成本；如果你不熟悉Hutool，那么本工具可以带你走进Hutool；但是如果你不熟悉Java，那使用本工具可能稍显困难。

## 终端命令参数解析

第一步，要实现一个终端命令，首先我们需要一个终端命令参数解析库，搜了一圈，发现Java的命令解析库还是挺多的，常用的有JCommander、CommonsCli、Args4J等，最后综合对比下来，选择了JCommander，因为它文档结构十分清晰，功能丰富，使用起来也非常简单顺手。

比如JCommander支持数组解析，我们可以将多个参数解析到同一个数组中，如 `--arr 1 --arr 2 --arr 3` 解析到 `@Parameter(names = "--arr") List<Integer> arr` 中，这特性可以应用到本工具执行静态方法时所需要的参数传入功能。

> [JCommander传送门](https://jcommander.org)

## 执行静态方法

有了终端命令参数解析的支持，那我们就可以开始设计命令参数了，要精确匹配静态方法，那么命令需要传入类全名、方法名、参数类型、方法参数值，通过传入的信息去匹配静态方法，并通过反射去调用它，最后将结果输出到终端。

根据上述步骤，用于方法定位的参数设计如下，其中为了简化输入，决定忽略方法名的大小写敏感，方法的参数类型也设计为非必填字段，参数类型为空时会根据具体的方法入参个数进行匹配调用。

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

有了第二步的加持，虽然现在我们可以精确的定位到一个方法，但是执行它还面临着一个问题，那就是我们通过终端传入的都是一连串的字符，但方法入参的类型可能是File、List、Date等等，调用方法时传入字符串会导致方法直接调用失败，所以我们还需要设计个方法参数转换器来解决这个问题。

转换器顶层接口设计如下，接口有两个方法需要实现，一个是将字符串转换为Java类型的对象，一个是将Java对象转换为字符串，用于将调用结果格式化输出。

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

例子1：文件转换器的实现，从下面代码中可以看出传入的字符串是文件路径，而且是支持相对路径的，命令示例：`-c cn.hutool.core.io.FileUtil -m readUtf8String -t java.io.File -p test.txt`。

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

例子2：日期转换器的实现，同样从下面代码可以看出，这里的日期除了支持常见的格式外，比如：yyyy-MM-dd, yyyy-MM-dd HH:mm:ss 等，还支持三个字符串：now, yesterday, tomorrow，简化输入。

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

因为本工具内置的转化器目前有限，为避免以后新增转换器带来升级问题，所以本工具设计为了支持动态引入参数转换器，可在 `converter.json` 文件添加具体的转化器类，并把class类文件放置到 `converter` 目录中即可。

> Java常用类型自动转换，无需引入方法参数转换器，比如：Boolean, Integer, Long, Float, Double等等，这些类型会使用fastjson的TypeUtils#cast方法转换。

## 别名设计

第四步，设计别名。

注意看前面的示例命令，有没有觉得很长？

如此冗长的命令，而且它竟然还需要你记住类名和方法名才能去执行，这也太难了吧，是我也放弃了。

所以这里少不别名的设计，而别名又有三个类型：

- 类方法别名：之前叫命令别名，感觉有歧义，所以更名为类方法别名，此类型别名的定义包括了类全名和方法名，以及方法入参类型，可以精确的匹配到一个静态方法。
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

第五步，优化静态方法的查看。

到这里，hutool-cli已经可以很方便地执行一个方法了，我们可以通过 `alias` 、 `methods` 等命令来输出静态方法的信息，包括方法名、参数类型等，但是对于一个方法来说，没有参数名称就感觉少了灵魂一样，我们不知道第一个参数是什么意思，第二个参数是什么意思，怎么传一脸懵逼，不去阅读Hutool源代码你是不会知道的，所以我们输出需要形参名称。

为了实现这个功能，这里引入 `javassist` 字节码库，这部分代码并不复杂，不做过多介绍，现在我们看到的就跟下面格式类似。

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

> 输出格式：`[className#]methodName(paramName:paramType[=defaultValue])`。

### 可执行的二进制文件

最后一步，从终端到Jar。

到这里，我们还只能通过 `java -jar hutool.jar -r alias` 命令来执行，完整的终端命令仍然很长，现在我们需要把 `java -jar hutool.jar` 这段干掉，则需要一个二进制可执行文件，它就像一个中转站，可以把从终端拿到的参数传给我们的Java程序，我选择了用 `golang` 来完成这项简单的工作（顺便学习下GO，哈哈）。

终于我们可以这样来执行了：`hu alias`，它还会传递一个参数，那就是将当前工作目录传递到 `--work-dir` 参数中。

> [Go源代码查看](src/main/go/hutool.go)

### 总结

本文简单介绍了本工具是如何一步一步设计完成的：从一个想法到实现， 从终端命令参数解析到静态方法的执行、参数转换器、别名的设计等等。

至此hutool-cli基本功能及其实现已全部介绍完成。

感谢阅读。
