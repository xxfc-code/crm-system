package com.crm.common;

/**
 * 业务编号 = 前缀 + 主键补零，例如 LD0001
 */
public class CodeUtil {

    public static String gen(String prefix, Long id) {
        return prefix + String.format("%04d", id);
    }
}
