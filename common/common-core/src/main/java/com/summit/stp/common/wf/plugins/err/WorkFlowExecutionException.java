package com.summit.stp.common.wf.plugins.err;

public class WorkFlowExecutionException extends RuntimeException {
    public WorkFlowExecutionException(Throwable message) {
        super(message);
    }
}
