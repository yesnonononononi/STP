package com.summit.stp.message.application.service.impl;

import com.summit.stp.common.util.DateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class MessageAppServiceImplTest {

    @Test
    void testParseEpochMilli() {
        String timestampStr = "1782375190889";
        Instant parsed = DateUtil.parse(timestampStr);
        Assertions.assertNotNull(parsed);
        Assertions.assertEquals(1782375190889L, parsed.toEpochMilli());
    }

    @Test
    void testParseIsoFormat() {
        String isoStr = "2026-06-25T08:13:10.889Z";
        Instant parsed = DateUtil.parse(isoStr);
        Assertions.assertNotNull(parsed);
        Assertions.assertEquals(1782375190889L, parsed.toEpochMilli());
    }

    @Test
    void testParseInvalidAndFallback() {
        Instant before = Instant.now();
        Instant parsedNull = DateUtil.parse(null);
        Instant parsedEmpty = DateUtil.parse("   ");
        Instant parsedInvalid = DateUtil.parse("not-a-date");
        Instant after = Instant.now();

        Assertions.assertNotNull(parsedNull);
        Assertions.assertNotNull(parsedEmpty);
        Assertions.assertNotNull(parsedInvalid);

        Assertions.assertTrue(parsedNull.toEpochMilli() >= before.toEpochMilli());
        Assertions.assertTrue(parsedNull.toEpochMilli() <= after.toEpochMilli());
    }
}
