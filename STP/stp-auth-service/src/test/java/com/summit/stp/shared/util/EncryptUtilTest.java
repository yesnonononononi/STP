package com.summit.stp.shared.util;

import com.summit.stp.common.util.EncryptUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptUtilTest {

    @Test
    void encodeStrForStar() {
        String s = EncryptUtil.encodeStrForStar("18573757527", "phone");
        System.out.println(s);
        assertEquals("185****7527", s);
    }
}