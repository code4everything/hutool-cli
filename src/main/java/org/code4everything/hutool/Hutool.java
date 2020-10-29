package org.code4everything.hutool;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.beust.jcommander.JCommander;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author pantao
 * @since 2020/10/27
 */
public class Hutool {

    private static final MethodArg ARG = new MethodArg();

    public static void main(String[] args) {
        JCommander commander = JCommander.newBuilder().addObject(ARG).build();
        commander.parse(args);
        debugOutput("received command line arguments: {}", Arrays.asList(args));
        // parse(ARG.className);
    }

    private static void parse(String className) {
        if (StrUtil.isEmpty(className)) {
            // TODO: 2020/10/30 show all supported hutool class full name
            return;
        }

        Class<?> clazz;

        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            debugOutput(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            parse(StrUtil.EMPTY);
            return;
        }

        getMethod(clazz, ARG.methodName);
    }

    private static void getMethod(Class<?> clazz, String methodName) {
        if (StrUtil.isEmpty(methodName)) {
            // TODO: 2020/10/30 show all static method of the specific hutool class
            return;
        }

        Method method = ReflectUtil.getMethodByNameIgnoreCase(clazz, methodName);
        if (Objects.isNull(method)) {
            debugOutput("static method not found(ignore case): {}#{}()", clazz.getName(), methodName);
            getMethod(clazz, StrUtil.EMPTY);
            return;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (ARG.params.size() < parameterTypes.length) {
            Console.log("parameter error, required: {}", Arrays.asList(parameterTypes));
            return;
        }

        ParserConfig parserConfig = new ParserConfig();
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            params[i] = TypeUtils.cast(ARG.params.get(i), parameterTypes[i], parserConfig);
        }
        Console.log(ReflectUtil.invokeStatic(method, params));
    }

    private static void debugOutput(String msg, Object... params) {
        if (ARG.debug) {
            Console.log(msg, params);
            Console.log();
        }
    }
}
