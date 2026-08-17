package com.summit.stp.common.constants;

public interface ImConstants {
    interface Business {
        int MAX_CONTENT_LENGTH = 1000;
    }

    interface Cache {
        String PREFIX = "ws:";
        String ONLINE_KEY = PREFIX + "online:";
        String CONNECTION_KEY = PREFIX + "connection:";

    }
}
