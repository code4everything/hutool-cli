package org.code4everything.hutool;

import cn.hutool.core.util.RandomUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.junit.Test;

/**
 * @author pantao
 * @since 2021/7/2
 */
public class UtilsTest {

    @Test
    public void getMethodFullInfo() throws NotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.getCtClass(RandomUtil.class.getName());
        CtMethod ctMethod = ctClass.getDeclaredMethod("randomLong", new CtClass[]{classPool.get(long.class.getName()), classPool.get(long.class.getName())});
        Utils.getMethodFullInfo(ctMethod, null);
    }
}
