package com.summit.stp.common.codec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdCodecTest {

    private final IdCodec idCodec = new SimpleIdCodec();

    @Test
    public void testEncodeDecodeSuccess() {
        Long[] testIds = {1L, 2L, 100L, 999999L, 123456789012345L, Long.MAX_VALUE};
        for (Long id : testIds) {
            String encoded = idCodec.encode(id);
            Assertions.assertNotNull(encoded);
            Long decoded = idCodec.decode(encoded);
            Assertions.assertEquals(id, decoded, "解码后的 ID 必须与原始 ID 一致");
        }
    }

    @Test
    public void testEncodeUniqueness() {
        String encoded1 = idCodec.encode(1L);
        String encoded2 = idCodec.encode(2L);
        Assertions.assertNotEquals(encoded1, encoded2, "不同 ID 的 publicId 应当不相等");
    }

    @Test
    public void testNullAndEmptyHandling() {
        Assertions.assertNull(idCodec.encode(null));
        Assertions.assertNull(idCodec.decode(null));
        Assertions.assertNull(idCodec.decode(""));
        Assertions.assertNull(idCodec.decode("   "));
    }

    @Test
    public void testInvalidPublicIdThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            idCodec.decode("InvalidPublicId!!!--");
        });
    }
}
