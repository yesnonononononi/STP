package com.summit.stp.common.application.domain.event;

import org.springframework.context.ApplicationEvent;
import java.util.List;

public class FileDeleteEvent extends ApplicationEvent {
    private final List<String> fileUrls;

    public FileDeleteEvent(Object source, List<String> fileUrls) {
        super(source);
        this.fileUrls = fileUrls;
    }

    public List<String> getFileUrls() {
        return fileUrls;
    }
}
