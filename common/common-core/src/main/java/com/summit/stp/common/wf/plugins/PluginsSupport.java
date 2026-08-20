package com.summit.stp.common.wf.plugins;

import com.summit.stp.common.wf.WorkFlow;

public interface PluginsSupport {
     WorkFlow.Step use(Plugin plugin, WorkFlow.Step that);
}
