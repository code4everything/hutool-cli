package org.code4everything.hutool;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author pantao
 * @since 2021/10/9
 */
public class Lottery {

    public static String lottery() {
        List<Integer> baseList = ListUtil.list(true, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33);
        StringBuilder sb = new StringBuilder();
        Arrays.asList(0, 0, 0, 0, 0, 0).stream().map(e -> {
            int idx = RandomUtil.randomInt(0, baseList.size());
            return baseList.remove(idx);
        }).sorted(Comparator.naturalOrder()).forEach(e -> {
            sb.append(StrUtil.padPre(String.valueOf(e), 2, '0')).append(" ");
        });
        return sb.append("| ").append(StrUtil.padPre(String.valueOf(RandomUtil.randomInt(0, 16) + 1), 2, '0')).toString();
    }
}
