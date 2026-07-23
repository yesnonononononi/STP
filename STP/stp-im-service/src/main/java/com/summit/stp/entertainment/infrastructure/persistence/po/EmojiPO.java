package com.summit.stp.entertainment.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("emoji")
public class EmojiPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long packageId;
    private String tiny;
    private String name;
    private Integer type;
    private  String url;
    private Timestamp createTime;
    private Timestamp updateTime;
}
