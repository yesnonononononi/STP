package com.summit.stp.common.application.service.TextSafe;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TextSafeServiceProviderImpl implements TextSafeServiceProvider{
    private final SensitiveWordBs sensitiveWordBs;

    private static final Safelist IMG_AND_A_SAFELIST = Safelist.none()
            .addTags("img", "a")
            .addAttributes("img", "src", "alt", "title")
            .addAttributes("a", "href", "title", "target")
            .addProtocols("img", "src", "http", "https")
            .addProtocols("a", "href", "http", "https");

    @Override
    public boolean sensitiveDetect(String text ) {
        return sensitiveWordBs.contains(text);
    }

    @Override
    public String xssFilter(String text) {
        if (text == null) {
            return null;
        }
        return Jsoup.clean(text, IMG_AND_A_SAFELIST);
    }
}



