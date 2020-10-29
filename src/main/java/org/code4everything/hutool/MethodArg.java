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

    private static final String PARAM_DESC = "the parameter(s) of method invoking required";

    private static final String EXCEPTION_DESC = "thrown an exception, only work on debug mode";

    // @formatter:on

    @Parameter(names = {"-r", "--run", "--command"}, description = "build in method", variableArity = true, order = 0)
    List<String> command;

    @Parameter(names = {"-c", "--class"}, description = CLASS_DESC, help = true, order = 1)
    String className;

    @Parameter(names = {"-m", "--method"}, description = "the static method name(ignore case)", help = true, order = 2)
    String methodName;

    @Parameter(names = {"-t", "--type"}, description = "the class type of parameter(not required)", order = 3)
    List<String> paramTypes = new ArrayList<>();

    @Parameter(names = {"-p", "--param", "--parameter"}, description = PARAM_DESC, order = 4)
    List<String> params = new ArrayList<>();

    @Parameter(names = {"-y", "--yank", "--copy"}, description = "copy result to clipboard", order = 5)
    boolean copy;

    @Parameter(names = {"-a", "--auto-param"}, description = "clipboard string into a parameter", order = 6)
    boolean paramFromClipboard;

    @Parameter(names = {"-v", "version"}, description = "the current version of hutool command line tool", order = 7)
    boolean version;

    @Parameter(names = {"-d", "--debug"}, description = "enable debug mode", order = 8)
    boolean debug;

    @Parameter(names = {"--exception"}, description = EXCEPTION_DESC, order = 9, hidden = true)
    boolean exception;
}
