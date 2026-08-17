package com.summit.stp.common.codec;

/**
 * 统一 ID 编解码器接口
 * 用于内部 Long ID 与外部 Public ID (String) 的相互转换
 */
public interface IdCodec {

    /**
     * 将内部 Long ID 转换为对外 Public ID
     *
     * @param id 内部 ID
     * @return 对外 Public ID 字符串
     */
    String encode(Long id);

    /**
     * 将对外 Public ID 解码为内部 Long ID
     *
     * @param value 对外 Public ID 字符串
     * @return 内部 Long ID
     */
    Long decode(String value);
}
