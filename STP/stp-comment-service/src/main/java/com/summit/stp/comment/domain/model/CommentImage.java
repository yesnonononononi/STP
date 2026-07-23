package com.summit.stp.comment.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentImage {
    private Long commentId;
    private Long id;
    
    @JsonProperty("sortOrder")
    @JsonAlias({"sortorder", "sort_order"})
    private Integer sortOrder;
    
    @JsonProperty("imageUrl")
    @JsonAlias({"imageurl", "image_url"})
    private String imageUrl;
    
    @JsonProperty("imageName")
    @JsonAlias({"imagename", "image_name"})
    private String imageName;
    
    private Integer width;
    private Integer height;
    
    @JsonProperty("typeCode")
    @JsonAlias({"typecode", "type_code"})
    private Integer typeCode;
    
    private Integer size;
}
