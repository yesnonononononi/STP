package com.summit.stp.shared.service.TextSafe;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TextSafeServiceProviderImpl implements TextSafeServiceProvider{
    private final SensitiveWordBs sensitiveWordBs;
    @Override
    public boolean sensitiveDetect(String text ) {
        return sensitiveWordBs.contains(text);
    }
}
