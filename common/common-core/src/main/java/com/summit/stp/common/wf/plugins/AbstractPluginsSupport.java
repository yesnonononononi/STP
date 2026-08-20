package com.summit.stp.common.wf.plugins;

import com.summit.stp.common.wf.WorkFlow;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Getter
public abstract class AbstractPluginsSupport implements PluginsSupport {
    List<Plugin> plugins = new LinkedList<>();

    @Override
    public WorkFlow.Step use(Plugin plugin, WorkFlow.Step that) {
        if (plugin != null) {
            plugins.add(plugin);
        }
        return that;
    }



    public boolean acquirePlugins(){
        return !this.plugins.isEmpty();
    }
}
