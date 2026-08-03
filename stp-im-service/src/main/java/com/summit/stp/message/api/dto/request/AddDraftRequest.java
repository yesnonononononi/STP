package com.summit.stp.message.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddDraftRequest {
    private String draft;
    private Long sessionId;
}
