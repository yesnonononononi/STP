package com.summit.stp.message.admin.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminCreateNotificationRequest {
   private String title;
   private String content;
   private Integer noticeType;
   private Integer targetType;
   private Long targetUserId;
   private List<String> imageUrls;
}
