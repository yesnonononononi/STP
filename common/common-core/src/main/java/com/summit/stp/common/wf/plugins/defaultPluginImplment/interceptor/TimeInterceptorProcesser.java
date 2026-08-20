package com.summit.stp.common.wf.plugins.defaultPluginImplment.interceptor;


import com.summit.stp.common.wf.plugins.interceptor.TimeInterceptor;


public class TimeInterceptorProcesser implements TimeInterceptor {
    @Override
    public void action(Runnable task) {
        long curTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        long res = (endTime-curTime ) ;
        System.out.printf("time interceptor cost %d s \n%n", res);
    }
}
