package org.code4everything.hutool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import org.code4everything.hutool.converter.FileConverter;
import org.code4everything.hutool.converter.LineSepConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pantao
 * @since 2021/6/29
 */
public class FileTree {

    @IOConverter(LineSepConverter.class)
    public static List<String> treeFile(@IOConverter(FileConverter.class) File file, int maxDepth) {
        if (!FileUtil.exist(file)) {
            return Collections.emptyList();
        }
        return treeFile(file, 1, maxDepth, true);
    }

    private static List<String> treeFile(File file, int currDepth, int maxDepth, boolean isLast) {
        if (maxDepth > -1 && currDepth > maxDepth) {
            return Collections.emptyList();
        }
        if (file.isFile()) {
            return Collections.singletonList((isLast ? "└─" : "├─") + file.getName());
        }

        File[] files = file.listFiles();
        if (ArrayUtil.isEmpty(files)) {
            return Collections.emptyList();
        }

        List<File> fileList = Arrays.stream(files).filter(f -> !f.isHidden() && (!f.isDirectory() || !f.getName().startsWith("."))).sorted(Comparator.comparing(File::isDirectory).reversed()).collect(Collectors.toList());
        if (Utils.isCollectionEmpty(fileList)) {
            return Collections.emptyList();
        }

        int last = fileList.size() - 1;
        List<String> list = new ArrayList<>();
        for (int i = 0; i <= last; i++) {
            File f = fileList.get(i);
            boolean isLastInner = i == last;
            boolean directory = f.isDirectory();
            if (directory) {
                list.add((isLastInner ? "└─" : "├─") + f.getName());
            }
            List<String> innerList = treeFile(f, directory ? currDepth + 1 : currDepth, maxDepth, isLastInner);
            if (directory) {
                list.addAll(innerList.stream().map(e -> (isLastInner ? " " : "│") + " " + e).collect(Collectors.toList()));
            } else {
                list.addAll(innerList);
            }
        }

        return list;
    }
}
