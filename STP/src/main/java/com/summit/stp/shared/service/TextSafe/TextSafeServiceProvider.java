package com.summit.stp.shared.service.TextSafe;

public interface TextSafeServiceProvider {

    /**
     * 敏感词检测
     * @param text 文本
     */
    boolean sensitiveDetect(String text);


    /**
     * xss过滤
     */
    String xssFilter(String text);
}
