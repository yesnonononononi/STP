package com.summit.stp.common.global;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StpMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {

        if (metaObject.hasGetter("publicId") ) {
            Object publicId = this.getFieldValByName("publicId", metaObject);
            if (publicId == null || StrUtil.isBlank(publicId.toString()) || publicId.toString().equals("undefined")) {
                log.info("【公共】自动填充 publicId");
                this.strictInsertFill(metaObject, "publicId", Long.class, IdUtil.getSnowflakeNextId());
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新操作暂无需要自动填充的公共字段
    }
}
