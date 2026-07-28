package com.summit.stp.shared.service.TextSafe;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.summit.stp.common.application.service.TextSafe.TextSafeServiceProviderImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TextSafeServiceProviderTest {

    @Test
    void testXssFilterAllowsSafeImgAndAAndFiltersScripts() {
        // Arrange
        SensitiveWordBs sensitiveWordBs = Mockito.mock(SensitiveWordBs.class);
        TextSafeServiceProviderImpl provider = new TextSafeServiceProviderImpl(sensitiveWordBs);

        // 1. 测试常规文本保留
        String rawText = "Hello World";
        Assertions.assertEquals("Hello World", provider.xssFilter(rawText));

        // 2. 测试 script 标签和 onerror 属性滤除，保留安全的 img 标签
        String badText = "Hello <script>alert(1)</script><img src=\"http://example.com/a.png\" onerror=\"alert(2)\">";
        // 期望：因为使用 Jsoup，script 标签内的所有内容都会被干净抹除，只保留安全图片
        String expected = "Hello <img src=\"http://example.com/a.png\">";
        Assertions.assertEquals(expected, provider.xssFilter(badText).trim());

        // 3. 测试 a 标签放行，且过滤 javascript 伪协议
        String linkText = "Link: <a href=\"http://google.com\">Google</a> <a href=\"javascript:alert(1)\">Evil</a>";
        String filteredLink = provider.xssFilter(linkText).trim();
        // 验证合法的 a 标签保留，非法的 javascript 协议链接被安全剥离其 href 或标签
        Assertions.assertTrue(filteredLink.contains("href=\"http://google.com\""));
        Assertions.assertFalse(filteredLink.contains("javascript:alert"));
        Assertions.assertTrue(filteredLink.contains("Google"));
        Assertions.assertTrue(filteredLink.contains("Evil"));
    }
}

