package com.summit.stp.shared.domain.model;

import com.summit.stp.shared.constant.UserAuthConstants;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.shared.util.EncryptUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Password {
    private final String value;
    private final boolean isEncrypted;

    private Password(String value, boolean isEncrypted) {
        this.value = value;
        this.isEncrypted = isEncrypted;
    }

    /**
     * 从原始密码创建（注册时使用）
     */
    public static Password fromRaw(String value) {
        validate(value);
        return new Password(value, false);
    }

    /**
     * 从数据库哈希值还原（查询时使用）
     */
    public static Password fromHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            throw new ParameterException("密码哈希不能为空");
        }
        return new Password(hash, true);
    }

    private static void validate(String value) {
        if (value == null || value.isEmpty()) {
            throw new ParameterException("密码不能为空");
        }
        if (value.length() < UserAuthConstants.MIN_PASSWORD_LENGTH) {
            throw new ParameterException(
                String.format("密码长度不能小于%d", UserAuthConstants.MIN_PASSWORD_LENGTH));
        }
        if (value.length() > UserAuthConstants.MAX_PASSWORD_LENGTH) {
            throw new ParameterException(
                String.format("密码长度不能大于%d", UserAuthConstants.MAX_PASSWORD_LENGTH));
        }
        if (!value.matches("^[a-zA-Z0-9_]+$")) {
            throw new ParameterException("密码只能包含字母、数字和下划线");
        }
    }

    /**
     * 获取 BCrypt 加密后的值（用于持久化存储）
     * 如果已经是加密的，直接返回；否则执行 BCrypt 哈希
     */
    public String getEncryptedValue() {
        return isEncrypted ? value : EncryptUtil.bcryptHash(value);
    }

    /**
     * 验证原始密码是否与当前（已加密的）密码匹配
     * @param rawPassword 用户输入的原始密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword) {
        if (!isEncrypted) {
            throw new IllegalStateException("只有从数据库加载的密码才能进行匹配验证");
        }
        return EncryptUtil.bcryptVerify(rawPassword, this.value);
    }

    @Override
    public String toString() {
        return "******";
    }
}
