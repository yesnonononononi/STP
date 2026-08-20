package com.summit.stp.common.wf.plugins;

import com.summit.stp.common.wf.WorkFlow;
@FunctionalInterface
public interface StepExecutor {
      void invoke(WorkFlow.Step step);
}
