package com.summit.stp.common.config;

import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class TextSafeConfig {
    @Bean
    public SensitiveWordBs sensitiveWordBs(){
        List<String> allowWords = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource("allow.txt");
            if (resource.exists()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            allowWords.add(line);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        IWordAllow wordAllow = WordAllows.chains(
                WordAllows.defaults(),
                () -> allowWords
        );

        return SensitiveWordBs.newInstance()
                .ignoreCase(true)          // 忽略大小写
                .ignoreWidth(true)         // 忽略全角/半角，例如处理"ｈｅｌｌｏ"
                .ignoreChineseStyle(true)  // 忽略中文书写格式（繁简体等）
                .ignoreEnglishStyle(true)  // 忽略英文书写格式

                .enableUrlCheck(true)      // 是否启用网址检测
                .wordAllow(wordAllow)      // 自定义白名单
                .init();                   // 初始化，必选项
    }
}
