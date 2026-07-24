package com.summit.stp.shared.handler;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StpMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasGetter("publicId") && this.getFieldValByName("publicId", metaObject) == null) {
            log.info("【公共】自动填充 publicId");
            this.strictInsertFill(metaObject, "publicId", Long.class, IdUtil.getSnowflakeNextId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新操作暂无需要自动填充的公共字段
    }
}
