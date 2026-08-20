package com.summit.stp.common.wf.plugins;

import com.summit.stp.common.wf.WorkFlow;
import com.summit.stp.common.wf.plugins.err.WorkFlowExecutionException;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class DefaultStepExecutor implements StepExecutor {
    private DefaultStepExecutor() {
    }

    @Getter
    enum Instance {
        INSTANCE;
        private final StepExecutor stepExecutor = new DefaultStepExecutor();
    }

    public static StepExecutor getInstance() {
        return Instance.INSTANCE.getStepExecutor();
    }

    public void invoke(WorkFlow.Step step) {
        Runnable chain = step.getTask();
        LinkedList<ErrorHandler> onError = step.getOnError();
        try {
            if (step.acquirePlugins()) {
                List<Plugin> plugins = step.getPlugins();

                for (int i = plugins.size()-1; i >= 0; i--) {
                    Plugin curPlu = plugins.get(i);
                    Runnable next = chain;
                    chain = () -> curPlu.action(next);
                }

                chain.run();
            } else {
                if (chain != null) {
                    chain.run();
                }
            }
        } catch (
                Exception e) {
            handleErr(e, onError);
        }


    }

    private void handleErr(Exception e, LinkedList<ErrorHandler> onError) {
        try {
            if (onError != null && !onError.isEmpty()) {
                boolean isHandled = false;

                for (ErrorHandler handler : onError) {
                    Class<? extends Exception> errorType = handler.getErrorType();
                    if (errorType.isInstance(e)) {
                        isHandled = true;
                        try {
                            handler.getHandle().accept(errorType.cast(e));
                        } catch (Exception exeE) {
                            exeE.addSuppressed(e);
                            throw exeE;
                        }
                        if (!handler.isContinueOnError()) throw e;

                    }
                }
                if (!isHandled) throw e;
            } else throw e;
        } catch (Exception ex) {
            throw new WorkFlowExecutionException(ex);
        }
    }


}
