package com.summit.stp.message.infrastructure.constants;

public interface ImConstants {
    interface Business {
        int MAX_CONTENT_LENGTH = 1000;
        String TEMPLATE_HTML_MSG = 
            "<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%%; gap: 12px;\">" +
            "    <div style=\"display: flex; align-items: center; gap: 12px;\">" +
            "        <img src=\"%s\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%%; object-fit: cover; border: 1.5px solid #3b82f6;\" />" +
            "        <div>" +
            "            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">%s</div>" +
            "            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">%s了你的帖子</div>" +
            "            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">%s</div>" +
            "        </div>" +
            "    </div>" +
            "    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">" +
            "        %s" +
            "    </div>" +
            "</div>";
    }

    interface Cache {
        String PREFIX = "ws:";
        String ONLINE_KEY = PREFIX + "online:";
        String CONNECTION_KEY = PREFIX + "connection:";
        String TOKEN_KEY = PREFIX + "token:";
    }
}
