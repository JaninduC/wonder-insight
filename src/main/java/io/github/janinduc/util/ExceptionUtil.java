package io.github.janinduc.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ExceptionUtil {
    public static String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    public static Map<String, Object> buildParamMap(String[] names, Object[] values) {
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            paramMap.put(names[i], values[i]);
        }
        return paramMap;
    }
}
