package com.summit.stp.shared.util;

import com.summit.stp.common.util.IpUtil;
import org.junit.jupiter.api.Test;

class IpUtilTest {

    @Test
    void testToString() throws Exception {
        IpUtil.toString("47.97.127.64");
    }
}