package com.bh.spider.scheduler.context;

import com.bh.spider.scheduler.Scheduler;
import com.bh.spider.scheduler.domain.ExtractFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalContext extends AbstractCloseableContext {
    private final static Logger logger = LoggerFactory.getLogger(LocalContext.class);
    private Scheduler scheduler;

    public LocalContext(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    @Override
    public void write(Object data) {

    }



    @Override
    public void exception(Throwable cause) {
        cause.printStackTrace();
    }



    @Override
    public void commandCompleted(Object data) {

    }


    protected ExtractFacade buildExtractFacade(Scheduler scheduler,Context ctx,String name) throws Exception {

        return ExtractFacade.facade(scheduler,ctx,name);
    }

}
