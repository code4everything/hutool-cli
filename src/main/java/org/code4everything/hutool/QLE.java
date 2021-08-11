package org.code4everything.hutool;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.parse.ExpressPackage;

/**
 * @author pantao
 * @since 2021/8/11
 */
public final class QLE {

    private QLE() {}

    public static Object run(String express) throws Exception {
        ExpressRunner runner = new ExpressRunner();
        ExpressPackage expressPackage = runner.getRootExpressPackage();

        Hutool.debugOutput("import default package");
        expressPackage.addPackage("com.alibaba.fastjson");
        expressPackage.addPackage("cn.hutool.core.util");
        expressPackage.addPackage("cn.hutool.core.collection");
        expressPackage.addPackage("cn.hutool.core.date");
        expressPackage.addPackage("cn.hutool.core.io");
        expressPackage.addPackage("cn.hutool.core.lang");
        expressPackage.addPackage("cn.hutool.core.map");

        DefaultContext<String, Object> context = new DefaultContext<>();
        Hutool.debugOutput("execute expression");
        return runner.execute(express, context, null, true, false);
    }
}
