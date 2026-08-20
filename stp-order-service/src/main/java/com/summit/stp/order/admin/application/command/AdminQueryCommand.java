package com.summit.stp.order.admin.application.command;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AdminQueryCommand {
   private Integer page;
   private Integer size;
   private String orderNo;
   private Long userId;
   private Integer orderType;
   private Integer payStatus;
}
