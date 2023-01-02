package vn.example.streaming.flink.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class StringUtils {
    private static final Pattern ARRAY_STR_REGEX = Pattern.compile("\\[(.*?)\\]");

    public static boolean notNullOrEmpty(String ss) {
        return !Strings.isNullOrEmpty(ss);
    }

    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static List<String> stringToList(String strArr) {
        String replace = strArr.replace("[", "");
        String replace1 = replace.replace("]", "");
        return new ArrayList<String>(Arrays.asList(replace1.split(",")));
    }

    public static String trimToNull(String ss) {
        String ts = ss == null ? null : ss.trim();
        return Strings.isNullOrEmpty(ts) ? null : ts;
    }

    public static Optional<String> trimToNullWithOpt(String ss) {
        String rs = trimToNull(ss);
        if (rs == null) {
            return Optional.empty();
        }

        return Optional.of(rs);
    }

    public static boolean isNotBlankAfterTrimming(String ss) {
        return isNotBlank(trimToNull(ss));
    }

    public static boolean isNotBlank(String ss) {
        return !Strings.isNullOrEmpty(ss);
    }

    public static String coalesce(String... items) {
        for (String i : items) {
            if (i != null) {
                return i;
            }
        }
        return null;
    }

    public static String coalesceWithEmpty(String... items) {
        for (String i : items) {
            if (!Strings.isNullOrEmpty(i)) {
                return i;
            }
        }
        return null;
    }

    public static String strip(String ss, Character c) {
        return CharMatcher.is(c).trimFrom(ss);
    }

    public static List<String> toListValueStr(String ss) {
        ss = trimToNull(ss);
        if (ss == null) {
            return Collections.emptyList();
        }

        if (ss.startsWith("[")) {
            Matcher m = ARRAY_STR_REGEX.matcher(ss);
            if (m.matches()) {
                ss = m.group(1);
            }
        }

        return Arrays.stream(ss.split(","))
                .map(is -> {
                    if ((is = org.apache.commons.lang3.StringUtils.trimToNull(is)) != null) {
                        is = org.apache.commons.lang3.StringUtils.strip(is, "\"");
                    }
                    return is;
                })
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    public static boolean isIn(String substring, String ss) {
        if (ss != null) {
            return ss.contains(substring);
        }
        return substring == null;
    }

    /**
     * lazy evaluate
     */
    @SafeVarargs
    public static String coalesce(Supplier<String>... suppliers) {
        for (Supplier<String> sup : suppliers) {
            String res = sup.get();
            if (!Strings.isNullOrEmpty(res)) {
                return res;
            }
        }

        return null;
    }

    public static long parseLong(String rawLong) {
        try {
            return Strings.isNullOrEmpty(rawLong) ? 0 : Long.parseLong(rawLong);
        } catch (Exception ignored) {
        }
        return 0L;
    }

    public static String[] convertStringToArr(String str, String delimiter) {
        return str.split(delimiter);
    }
}
