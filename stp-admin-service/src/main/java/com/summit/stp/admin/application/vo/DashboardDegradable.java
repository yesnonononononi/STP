package com.summit.stp.admin.application.vo;

/**
 * 可降级标记接口。
 *
 * <p>实现该接口的大盘 VO 在「远程依赖取数失败」时会通过 {@link #getDegraded()} 返回 {@code true}，
 * 用于区分「真实的 0」与「取数失败」：缓存层据此缩短 TTL，避免把失败态缓存为真实数据 3 分钟。</p>
 *
 * <p>字段命名为 {@code degraded} 且使用 {@code Boolean} 类型，是为了让 Lombok 的
 * {@code @Getter}/{@code @Data} 自动生成 {@code getDegraded()} 以满足本接口约定。</p>
 */
public interface DashboardDegradable {

    /**
     * @return true 表示当前数据为降级结果（部分或全部远程依赖取数失败），null/false 表示正常
     */
    Boolean getDegraded();
}
