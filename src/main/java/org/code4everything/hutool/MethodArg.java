package org.code4everything.hutool;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pantao
 * @since 2020/10/28
 */
public class MethodArg {

    // @formatter:off

    private static final String CLASS_DESC = "the hutool class name, here will add prefix 'cn.hutool.' automatically if missed";

    // @formatter:on

    @Parameter(names = {"-c", "--class"}, description = CLASS_DESC, help = true)
    String className;

    @Parameter(names = {"-m", "--method"}, description = "the static method name(ignore case)", help = true)
    String methodName;

    @Parameter(names = {"-p", "--param", "--parameter"}, description = "the parameter(s) of method invoking required")
    List<String> params = new ArrayList<>();

    @Parameter(names = {"-t", "--type"}, description = "the class type of parameter(not required)")
    List<String> paramTypes = new ArrayList<>();

    @Parameter(names = {"-r", "--run", "--command"}, description = "build in method", variableArity = true)
    List<String> command;

    @Parameter(names = {"-y", "--yank", "--copy"}, description = "copy result in clipboard")
    boolean copy;

    @Parameter(names = {"-d", "--debug"}, description = "enable debug mode")
    boolean debug;
}
