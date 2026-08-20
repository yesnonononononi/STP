package com.summit.stp.common.wf.plugins;

import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;
@Data
@Builder
public class ErrorHandler {
    private Class<? extends Exception> errorType;
    private Consumer<Exception> handle;
    private boolean continueOnError;
}
