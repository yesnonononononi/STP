package com.summit.stp.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.sql.Timestamp;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "posts")
public class PostDocument implements Serializable {

    @Field(type = FieldType.Long)
    private Long id;

    @Field(
            type = FieldType.Text,
            analyzer = "ik_max_word",
            searchAnalyzer = "ik_smart"
    )
    private String title;

    @Field(
            type = FieldType.Text,
            analyzer = "ik_max_word",
            searchAnalyzer = "ik_smart"
    )
    private String content;

    @Field(type = FieldType.Date)
    private Timestamp createTime;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Long)
    private Long comment;

    @Field(type = FieldType.Long)
    private Long likeCount;

    @Field(type = FieldType.Date)
    private Timestamp updateTime;

    @Field(type = FieldType.Long)
    private Long creatorId;
}