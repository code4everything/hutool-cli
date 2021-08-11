package org.code4everything.hutool;

import cn.hutool.core.util.RuntimeUtil;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;

import java.util.List;

/**
 * @author pantao
 * @since 2021/8/11
 */
public final class QLE {

    private QLE() {}

    public static Object run(String express) throws Exception {
        List<String> params = MethodArg.getSubParams(Hutool.ARG, 1);
        for (int i = 0; i < params.size(); i++) {
            express = express.replace("${" + i + "}", params.get(i));
        }
        ExpressRunner runner = new ExpressRunner();
        runner.addFunctionOfClassMethod("cmd", QLE.class, "cmd", new Class<?>[]{String.class}, null);
        DefaultContext<String, Object> context = new DefaultContext<>();
        Hutool.debugOutput("execute expression");
        return runner.execute(express, context, null, true, false);
    }

    public static String cmd(String cmd) {
        String result = RuntimeUtil.execForStr(cmd);
        return Utils.isStringEmpty(result) ? "" : result.trim();
    }
}
