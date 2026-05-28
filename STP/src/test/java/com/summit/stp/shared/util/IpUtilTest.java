package com.summit.stp.shared.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IpUtilTest {

    @Test
    void testToString() throws Exception {
        IpUtil.toString("47.97.127.64");
    }
}