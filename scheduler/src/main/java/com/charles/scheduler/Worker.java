package com.charles.scheduler;

import com.charles.scheduler.event.MultiEventLoop;

/**
 * Created by lq on 17-3-16.
 */
public class Worker {
    private String address;
    private int port;
    private String hostname;
    private String system;
    private String version;

    private MultiEventLoop loop =null;
    private BasicScheduler scheduler=null;
    public Worker(BasicScheduler scheduler){
        this.scheduler=scheduler;
    }


    public void exec(){

    }
}
