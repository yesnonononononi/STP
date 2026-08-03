package com.summit.stp.common.infrastructure.websocket.provide;

public interface CacheProvider {
    /**
     * 设置用户在线状态
     * @param uid 用户ID
     * @param ticket 连接凭证 (SessionId)
     */
    void setOnline(Long uid, String ticket);

    /**
     * 获取用户在线凭证 (SessionId)
     * @param uid 用户ID
     * @return 连接凭证，离线时返回 null
     */
    String getTicket(Long uid);

    /**
     * 移除用户在线状态 (仅当当前 ticket 匹配时)
     * @param uid 用户ID
     * @param ticket 当前断开的凭证
     */
    void removeOnline(Long uid, String ticket);

    /**
     * 校验 Token 是否有效
     * @param token 握手 Token
     * @return 是否有效
     */
    boolean auth(String token);
}
