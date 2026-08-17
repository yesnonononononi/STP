package com.summit.stp.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "user")
public class UserDocument implements Serializable {
    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = @InnerField(type = FieldType.Keyword,suffix = "keyword")
    )
    private String nick;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String phone;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String ip;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Boolean)
    private Boolean isVip;

    @Field(type = FieldType.Integer)
    private Integer age;

    @Field(type = FieldType.Integer)
    private Integer gender;

    @Field(type = FieldType.Integer)
    private Integer vipLevel;

    @Field(type = FieldType.Integer)
    private Integer statusCode;
}
