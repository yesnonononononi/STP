package com.summit.stp.admin.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Builder
@Getter
public class Admin {
    private final Long id;
    private final Long userId;
    private final String username;
    private Integer status;
    private Integer order;
    final private Instant createTime;
    private Instant updateTime;

    public void descend(int delta) {
        this.order = Math.max(this.order - delta, 0);
        this.updateTime = Instant.now();
    }

    public void ascend(int delta) {
        this.order += delta;
        this.updateTime = Instant.now();
    }

    public void ban(){
        this.status = 0;
        this.updateTime = Instant.now();
    }

    public void unban(){
        this.status = 1;
        this.updateTime = Instant.now();
    }

    public boolean can(Admin admin){
        return this.order > Objects.requireNonNullElse(admin.getOrder(),0);
    }
}
