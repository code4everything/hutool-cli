package org.code4everything.hutool;

import java.util.Date;
import java.util.Objects;

/**
 * @author pantao
 * @since 2020/10/30
 */
public final class Utils {

    private Utils() {}

    public static long date2Millis(Date date) {
        return Objects.isNull(date) ? 0 : date.getTime();
    }
}
