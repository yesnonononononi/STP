package com.summit.stp.common.wf.plugins;
@FunctionalInterface
public interface Plugin {
    void action(Runnable step);
}
