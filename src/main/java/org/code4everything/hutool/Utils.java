package org.code4everything.hutool;

import cn.hutool.core.comparator.ComparatorChain;
import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Holder;
import cn.hutool.core.lang.JarClassLoader;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.LocalVariableAttribute;
import org.code4everything.hutool.converter.ClassConverter;
import org.code4everything.hutool.converter.DateConverter;
import org.code4everything.hutool.converter.FileConverter;
import org.code4everything.hutool.converter.ListStringConverter;
import org.code4everything.hutool.converter.PatternConverter;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author pantao
 * @since 2020/10/30
 */
public final class Utils {

    private static JSONObject classAliasJson = null;

    private static JarClassLoader classLoader = null;

    private static List<String> mvnRepositoryHome = null;

    private Utils() {}

    public static String listFiles(@IOConverter(FileConverter.class) File file) {
        if (!FileUtil.exist(file)) {
            return "file not found!";
        }
        if (FileUtil.isFile(file)) {
            return DateUtil.formatDateTime(new Date(file.lastModified())) + "\t" + FileUtil.readableFileSize(file) + "\t" + file.getName();
        }

        File[] files = file.listFiles();
        if (Objects.isNull(files) || files.length == 0) {
            return "";
        }

        Arrays.sort(files, ComparatorChain.of(Comparator.comparingInt(f -> f.isDirectory() ? 0 : 1), Comparator.comparing(File::getName)));
        StringJoiner joiner = new StringJoiner("\n");
        int maxLen = 0;
        String[] size = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            size[i] = FileUtil.readableFileSize(files[i]);
            if (size[i].length() > maxLen) {
                maxLen = size[i].length();
            }
        }

        for (int i = 0; i < files.length; i++) {
            file = files[i];
            joiner.add(DateUtil.formatDateTime(new Date(file.lastModified())) + "\t" + StrUtil.padPre(size[i], maxLen, " ") + "\t" + file.getName());
        }

        return joiner.toString();
    }

    public static String dayProcess(@IOConverter(DateConverter.class) DateTime specificDate) {
        DateTime date = DateUtil.beginOfDay(specificDate);
        double todayProcess = (specificDate.getTime() - date.getTime()) * 100 / 24D / 60 / 60 / 1000;
        Week weekEnum = DateUtil.dayOfWeekEnum(date);
        int week = weekEnum.getValue() - 1;
        week = (week == 0 ? 7 : week) * 24 - 24;
        double weekProcess = (week + specificDate.hour(true)) * 100 / 7D / 24;
        double monthProcess = DateUtil.dayOfMonth(date) * 100 / (double) DateUtil.endOfMonth(date).dayOfMonth();
        double yearProcess = DateUtil.dayOfYear(date) * 100 / (double) DateUtil.endOfYear(date).dayOfYear();

        String template = String.format("%s %s %s%n", lunar(specificDate), weekEnum.toChinese("周"), Hutool.getSimpleDateFormat().format(specificDate));
        template += String.format("%n今日 [%s]: %05.2f%%", getDayProcessString(todayProcess), todayProcess);
        template += String.format("%n本周 [%s]: %05.2f%%", getDayProcessString(weekProcess), weekProcess);
        template += String.format("%n本月 [%s]: %05.2f%%", getDayProcessString(monthProcess), monthProcess);
        template += String.format("%n本年 [%s]: %05.2f%%", getDayProcessString(yearProcess), yearProcess);
        return template;
    }

    private static String getDayProcessString(@IOConverter double process) {
        int p = (int) (Math.ceil(process * 100) / 100);
        char[] cs = new char[100];
        Arrays.fill(cs, 0, p, 'o');
        Arrays.fill(cs, p, 100, ' ');
        return new String(cs);
    }

    public static String toHttpUrlString(Map<String, ?> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String sep = "?";
        for (Map.Entry<String, ?> entry : paramMap.entrySet()) {
            if (Objects.isNull(entry.getValue())) {
                continue;
            }
            String value = entry.getValue().toString();
            if (isStringEmpty(value)) {
                continue;
            }
            sb.append(sep).append(entry.getKey()).append("=").append(value);
            sep = "&";
        }
        return sb.toString();
    }

    public static String getFieldNames(@IOConverter(ClassConverter.class) Class<?> clazz) throws Exception {
        if (clazz.isPrimitive()) {
            clazz = parseClass0(parseClassName0("j." + clazz.getName()));
        }

        Field[] fields = ReflectUtil.getFields(clazz);
        StringJoiner joiner = new StringJoiner("\n");
        Map<String, List<String>> modifierMap = new HashMap<>();
        ComparatorChain<String> comparators = new ComparatorChain<>();
        comparators.addComparator(Comparator.comparingInt(o -> modifierMap.computeIfAbsent(o, s -> {
            String[] ss = s.split(" ");
            return ss.length > 2 ? Arrays.stream(ss).limit(ss.length - 2L).collect(Collectors.toList()) : Collections.emptyList();
        }).size()));
        comparators.addComparator(Comparator.naturalOrder());

        List<String> modifierList = new ArrayList<>();
        Holder<String> holder = Holder.of("");
        Arrays.stream(fields).map(field -> {
            String line = "";
            int modifiers = field.getModifiers();
            if (Modifier.isPrivate(modifiers)) {
                line += "private ";
            } else if (Modifier.isProtected(modifiers)) {
                line += "protected ";
            } else if (Modifier.isPublic(modifiers)) {
                line += "public ";
            }

            if (Modifier.isStatic(modifiers)) {
                line += "static ";
            }
            if (Modifier.isFinal(modifiers)) {
                line += "final ";
            }
            if (Modifier.isTransient(modifiers)) {
                line += "transient ";
            }
            if (Modifier.isVolatile(modifiers)) {
                line += "volatile ";
            }
            return line + field.getType().getSimpleName() + " " + field.getName();
        }).sorted(comparators).forEach(s -> {
            String line = s;
            List<String> list = modifierMap.get(s);
            if (!modifierList.equals(list)) {
                line = holder.get() + line;
                modifierList.clear();
                modifierList.addAll(list);
            }

            joiner.add(line);
            holder.set("\n");
        });

        return joiner.toString();
    }

    public static Object getStaticFieldValue(@IOConverter(ClassConverter.class) Class<?> clazz, String fieldName) throws Exception {
        if (clazz.isPrimitive()) {
            clazz = parseClass0(parseClassName0("j." + clazz.getName()));
        }
        Field field = ReflectUtil.getField(clazz, fieldName);
        return ReflectUtil.getStaticFieldValue(field);
    }

    @IOConverter(ListStringConverter.class)
    public static List<String> getMatchedItems(@IOConverter(PatternConverter.class) Pattern regex, String content) {
        ArrayList<String> result = new ArrayList<>();
        Matcher matcher = regex.matcher(content);
        while (matcher.find()) {
            result.add(matcher.group(0));
        }
        return result;
    }

    public static String getSupperClass(@IOConverter(ClassConverter.class) Class<?> clazz) {
        StringJoiner joiner = new StringJoiner("\n");
        getSupperClass(joiner, "", clazz);
        return joiner.toString();
    }

    private static void getSupperClass(StringJoiner joiner, String prefix, Class<?> clazz) {
        if (clazz == null || Object.class == clazz) {
            return;
        }

        joiner.add(prefix + (clazz.isInterface() ? "<>" : "") + clazz.getName());

        getSupperClass(joiner, prefix + "|    ", clazz.getSuperclass());
        for (Class<?> anInterface : clazz.getInterfaces()) {
            getSupperClass(joiner, prefix + "|    ", anInterface);
        }
    }

    public static boolean assignableFrom(@IOConverter(ClassConverter.class) Class<?> sourceClass, @IOConverter(ClassConverter.class) Class<?> testClass) {
        return sourceClass.isAssignableFrom(testClass);
    }

    public static String calc(String expression, @IOConverter int scale) {
        double res = Calculator.conversion(expression);
        return String.format("%." + scale + "f", res);
    }

    public static String lunar(@IOConverter(DateConverter.class) Date date) {
        return new ChineseDate(date).toString();
    }

    public static long date2Millis(@IOConverter(DateConverter.class) Date date) {
        return Objects.isNull(date) ? 0 : date.getTime();
    }

    public static String toUpperCase(String str) {
        return isStringEmpty(str) ? "" : str.toUpperCase();
    }

    public static String toLowerCase(String str) {
        return isStringEmpty(str) ? "" : str.toLowerCase();
    }

    public static String grep(@IOConverter(PatternConverter.class) Pattern pattern, @IOConverter(ListStringConverter.class) List<String> lines) {
        StringJoiner joiner = new StringJoiner("\n");
        for (String line : lines) {
            if (pattern.matcher(line).find()) {
                joiner.add(line);
            }
        }
        return joiner.toString();
    }

    static <T> boolean isArrayEmpty(T[] arr) {
        return arr == null || arr.length == 0;
    }

    public static Class<?> parseClass(String className) throws Exception {
        switch (className) {
            case "bool":
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "char":
                return char.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                return parseClass0(className);
        }
    }

    private static Class<?> parseClass0(String className) throws Exception {
        className = parseClassName0(className);
        try {
            return Class.forName(className);
        } catch (Exception e) {
            // ignore
        }

        Hutool.debugOutput("loading class: " + className);
        if (Objects.isNull(classLoader)) {
            classLoader = new JarClassLoader();
            classLoader.addJar(FileUtil.file(Hutool.homeDir, "external"));
            File externalConf = FileUtil.file(Hutool.homeDir, "external.conf");
            if (FileUtil.exist(externalConf)) {
                String[] externalPaths = FileUtil.readUtf8String(externalConf).split(",");
                for (String externalPath : externalPaths) {
                    File external = parseClasspath(externalPath);
                    if (FileUtil.exist(external)) {
                        Hutool.debugOutput("add class path: " + external.getAbsolutePath());
                        classLoader.addURL(external);
                    }
                }
            }
        }

        return classLoader.loadClass(className);
    }

    private static File parseClasspath(String path) {
        path = path.trim();
        if (isStringEmpty(path)) {
            return null;
        }

        if (path.startsWith("mvn:")) {
            path = path.substring(4);
            if (isStringEmpty(path)) {
                return null;
            }
            String[] coordinates = path.split(":", 3);
            if (coordinates.length != 3) {
                Hutool.debugOutput("mvn coordinate format error: " + path);
                return null;
            }
            if (Objects.isNull(mvnRepositoryHome)) {
                mvnRepositoryHome = Arrays.asList("~", ".m2", "repository");
            }
            List<String> paths = new ArrayList<>(mvnRepositoryHome);
            paths.addAll(Arrays.asList(coordinates[0].split("\\.")));
            String name = coordinates[1];
            String version = coordinates[2];
            paths.add(name);
            paths.add(version);
            paths.add(name + "-" + version + ".jar");
            File file = FileUtil.file(paths.toArray(new String[0]));
            Hutool.debugOutput("get mvn path: " + file.getAbsolutePath());
            return file;
        }

        return FileUtil.file(path);
    }

    public static String parseClassName(String className) {
        switch (className) {
            case "bool":
            case "boolean":
                return "boolean";
            case "byte":
                return "byte";
            case "short":
                return "short";
            case "char":
                return "char";
            case "int":
                return "int";
            case "long":
                return "long";
            case "float":
                return "float";
            case "double":
                return "double";
            default:
                return parseClassName0(className);
        }
    }

    private static String parseClassName0(String className) {
        switch (className) {
            case "string":
                return "java.lang.String";
            case "j.char.seq":
                return "java.lang.CharSequence";
            case "file":
                return "java.io.File";
            case "charset":
                return "java.nio.charset.Charset";
            case "date":
                return "java.util.Date";
            case "class":
                return "java.lang.Class";
            case "j.bool":
            case "j.boolean":
                return "java.lang.Boolean";
            case "j.byte":
                return "java.lang.Byte";
            case "j.short":
                return "java.lang.Short";
            case "j.int":
            case "j.integer":
                return "java.lang.Integer";
            case "j.char":
                return "java.lang.Character";
            case "j.long":
                return "java.lang.Long";
            case "j.float":
                return "java.lang.Float";
            case "j.double":
                return "java.lang.Double";
            case "reg.pattern":
                return "java.util.regex.Pattern";
            case "map":
                return "java.util.Map";
            case "list":
                return "java.util.List";
            case "set":
                return "java.util.Set";
            default:
                return parseClassName00(className);
        }
    }

    private static String parseClassName00(String className) {
        if (className.endsWith(";") && className.contains("[L")) {
            int idx = className.indexOf("L");
            return className.substring(0, idx + 1) + parseClassName(className.substring(idx + 1, className.length() - 1)) + ";";
        }
        if (className.length() > 16) {
            return className;
        }
        if (classAliasJson == null) {
            classAliasJson = Hutool.getAlias("", Hutool.homeDir, Hutool.CLASS_JSON);
            classAliasJson.putAll(Hutool.getAlias("", "", Hutool.CLASS_JSON));
        }
        JSONObject classJson = classAliasJson.getJSONObject(className);
        if (classJson != null) {
            String className0 = classJson.getString(Hutool.CLAZZ_KEY);
            if (!isStringEmpty(className0)) {
                className = className0;
            }
        }
        return className;
    }

    public static boolean isStringEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static <T> boolean isCollectionEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    static String addPrefixIfNot(String str, String prefix) {
        if (isStringEmpty(str) || isStringEmpty(prefix) || str.startsWith(prefix)) {
            return str;
        }
        return prefix + str;
    }

    static String addSuffixIfNot(String str, String suffix) {
        if (isStringEmpty(str) || isStringEmpty(suffix) || str.endsWith(suffix)) {
            return str;
        }
        return str + suffix;
    }

    public static String padAfter(String str, int len, char pad) {
        if (str == null) {
            str = "";
        }

        int diff = len - str.length();
        if (diff < 1) {
            return str;
        }

        char[] cs = new char[diff];
        for (int i = 0; i < diff; i++) {
            cs[i] = pad;
        }

        return str + new String(cs);
    }

    public static String outputPublicStaticMethods(String className) {
        return isStringEmpty(className) ? "" : outputPublicStaticMethods0(className);
    }

    private static String outputPublicStaticMethods0(String className) {
        ClassPool pool = ClassPool.getDefault();
        StringJoiner joiner = new StringJoiner("\n");
        try {
            CtClass ctClass = pool.get(parseClassName(className));
            CtMethod[] methods = ctClass.getMethods();
            List<String> lineList = new ArrayList<>(methods.length);
            for (CtMethod method : methods) {
                int modifiers = method.getModifiers();
                if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                    continue;
                }
                lineList.add(getMethodFullInfo(method, null));
            }
            lineList.stream().sorted(String::compareTo).forEach(joiner::add);
        } catch (Exception e) {
            Hutool.debugOutput("parse class static methods error: %s", ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
        }

        return joiner.toString();
    }

    static String getMethodFullInfo(CtMethod method, Map<String, String> defaultValueMap) throws NotFoundException {
        StringJoiner paramJoiner = new StringJoiner(", ");
        LocalVariableAttribute attribute = (LocalVariableAttribute) method.getMethodInfo().getCodeAttribute().getAttribute(LocalVariableAttribute.tag);

        CtClass[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            CtClass parameterType = parameterTypes[i];
            String paramType = parameterType.getName();

            String paramStr = attribute.variableName(i) + ":" + paramType;
            if (defaultValueMap != null) {
                String defaultValue = defaultValueMap.get(paramType + i);
                if (!isStringEmpty(defaultValue)) {
                    paramStr += "=" + defaultValue;
                }
            }
            paramJoiner.add(paramStr);
        }

        return method.getName() + "(" + paramJoiner + ")";
    }
}
