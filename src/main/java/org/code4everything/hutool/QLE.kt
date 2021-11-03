package org.code4everything.hutool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;

import java.io.File;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author pantao
 * @since 2021/8/11
 */
public final class QLE {

    private QLE() {}

    public static Object run(String express, boolean replaceArg) throws Exception {
        express = handleExpression(express);
        if (Utils.isStringEmpty(express)) {
            return null;
        }

        List<String> args = MethodArg.getSubParams(Hutool.ARG, 2);
        if (replaceArg) {
            for (int i = 0; i < args.size(); i++) {
                express = express.replace("${" + i + "}", args.get(i));
            }
        }

        Hutool.debugOutput("get ql script: %s", express);
        ExpressRunner runner = new ExpressRunner();

        // 绑定方法
        runner.addFunctionOfClassMethod("cmd", QLE.class, "cmd", new Class<?>[]{String.class}, null);
        runner.addFunctionOfClassMethod("nullto", QLE.class, "nullTo", new Class<?>[]{Object.class, Object.class}, null);

        // 绑定上下文环境
        DefaultContext<String, Object> context = new DefaultContext<>();
        context.put("args", ArgList.of(new ArrayList<>(args)));
        StringJoiner joiner = new StringJoiner(",", "(", ")");
        for (int i = 0; i < args.size(); i++) {
            context.put("arg" + i, args.get(i));
            joiner.add(args.get(i));
        }

        // 执行表达式
        Hutool.debugOutput("execute expression with args: %s", joiner);
        return runner.execute(express, context, null, true, false);
    }

    public static String cmd(String cmd) {
        String result = RuntimeUtil.execForStr(cmd);
        return Utils.isStringEmpty(result) ? "" : result.trim();
    }

    public static Object nullTo(Object v1, Object v2) {
        return v1 == null ? v2 : v1;
    }

    private static String handleExpression(String expression) {
        if (Utils.isStringEmpty(expression)) {
            return "";
        }

        if (expression.startsWith("file:")) {
            File file = FileUtil.file(expression.substring(5));
            if (FileUtil.exist(file)) {
                Hutool.debugOutput("get script from file: %s", file.getAbsolutePath());
                return FileUtil.readUtf8String(file);
            }
        }

        return expression;
    }

    private static class ArgList extends JSONArray {

        public ArgList(List<Object> list) {
            super(list);
        }

        private static ArgList of(List<Object> list) {
            return new ArgList(list);
        }

        @Override
        public Object get(int index) {
            return index >= size() ? null : super.get(index);
        }

        @Override
        public JSONObject getJSONObject(int index) {
            return index >= size() ? null : super.getJSONObject(index);
        }

        @Override
        public JSONArray getJSONArray(int index) {
            return index >= size() ? null : super.getJSONArray(index);
        }

        @Override
        public <T> T getObject(int index, Class<T> clazz) {
            return index >= size() ? null : super.getObject(index, clazz);
        }

        @Override
        public <T> T getObject(int index, Type type) {
            return index >= size() ? null : super.getObject(index, type);
        }

        @Override
        public Boolean getBoolean(int index) {
            return index >= size() ? null : super.getBoolean(index);
        }

        @Override
        public boolean getBooleanValue(int index) {
            return getBooleanValue(index, false);
        }

        public boolean getBooleanValue(int index, boolean value) {
            return index >= size() ? value : super.getBooleanValue(index);
        }

        @Override
        public Byte getByte(int index) {
            return index >= size() ? null : super.getByte(index);
        }

        @Override
        public byte getByteValue(int index) {
            byte value = 0;
            return getByteValue(index, value);
        }

        public byte getByteValue(int index, byte value) {
            return index >= size() ? value : super.getByteValue(index);
        }

        @Override
        public Short getShort(int index) {
            return index >= size() ? null : super.getShort(index);
        }

        @Override
        public short getShortValue(int index) {
            short value = 0;
            return getShortValue(index, value);
        }

        public short getShortValue(int index, short value) {
            return index >= size() ? value : super.getShortValue(index);
        }

        @Override
        public Integer getInteger(int index) {
            return index >= size() ? null : super.getInteger(index);
        }

        @Override
        public int getIntValue(int index) {
            return getIntValue(index, 0);
        }

        public int getIntValue(int index, int value) {
            return index >= size() ? value : super.getIntValue(index);
        }

        @Override
        public Long getLong(int index) {
            return index >= size() ? null : super.getLong(index);
        }

        @Override
        public long getLongValue(int index) {
            return getLongValue(index, 0);
        }

        public long getLongValue(int index, long value) {
            return index >= size() ? value : super.getLongValue(index);
        }

        @Override
        public Float getFloat(int index) {
            return index >= size() ? null : super.getFloat(index);
        }

        @Override
        public float getFloatValue(int index) {
            return getFloatValue(index, 0);
        }

        public float getFloatValue(int index, float value) {
            return index >= size() ? value : super.getFloatValue(index);
        }

        @Override
        public Double getDouble(int index) {
            return index >= size() ? null : super.getDouble(index);
        }

        @Override
        public double getDoubleValue(int index) {
            return getDoubleValue(index, 0);
        }

        public double getDoubleValue(int index, double value) {
            return index >= size() ? value : super.getDoubleValue(index);
        }

        @Override
        public BigDecimal getBigDecimal(int index) {
            return index >= size() ? null : super.getBigDecimal(index);
        }

        @Override
        public BigInteger getBigInteger(int index) {
            return index >= size() ? null : super.getBigInteger(index);
        }

        @Override
        public String getString(int index) {
            return index >= size() ? null : super.getString(index);
        }

        @Override
        public Date getDate(int index) {
            return index >= size() ? null : super.getDate(index);
        }

        @Override
        public java.sql.Date getSqlDate(int index) {
            return index >= size() ? null : super.getSqlDate(index);
        }

        @Override
        public Timestamp getTimestamp(int index) {
            return index >= size() ? null : super.getTimestamp(index);
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
