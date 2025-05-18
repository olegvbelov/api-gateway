package com.olegvbelov.budget.apigateway.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {
    public static String stringValue(Object obj) {
        if (obj != null) {
            try {
                return (String) obj;
            } catch (Exception exp) {
                return obj.toString();
            }
        }
        return "";
    }

    public static String nvl(Object str, String defaultValue) {
        var string = stringValue(str);
        return string.isEmpty() ? defaultValue : string;
    }
}
