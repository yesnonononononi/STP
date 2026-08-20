package com.summit.stp.elasticsearch.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "posts")
public class PostDocument implements Serializable {
    @Id
    private Long id;


    @Field(type = FieldType.Long)
    private Long postId;
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
    private Date createTime;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Long)
    private Long comment;

    @Field(type = FieldType.Long)
    private Long likeCount;

    @Field(type = FieldType.Date)
    private Date updateTime;

    @Field(type = FieldType.Long)
    private Long creatorId;
}