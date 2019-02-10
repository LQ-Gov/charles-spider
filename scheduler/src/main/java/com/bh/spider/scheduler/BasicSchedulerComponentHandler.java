package com.bh.spider.scheduler;

import com.bh.spider.scheduler.component.ComponentCoreFactory;
import com.bh.spider.scheduler.component.ComponentRepository;
import com.bh.spider.scheduler.config.Config;
import com.bh.spider.scheduler.context.Context;
import com.bh.spider.scheduler.event.EventMapping;
import com.bh.spider.scheduler.event.IAssist;
import com.bh.spider.transfer.entity.Component;

import java.io.IOException;
import java.util.List;


public class BasicSchedulerComponentHandler implements IAssist {

    private ComponentCoreFactory factory = null;


    public BasicSchedulerComponentHandler(Config cfg, BasicScheduler scheduler) throws IOException {
        this(cfg,scheduler,new ComponentCoreFactory(cfg));
    }

    public BasicSchedulerComponentHandler(Config cfg,BasicScheduler scheduler,ComponentCoreFactory factory){
        this.factory = factory;
    }


    @EventMapping
    public void SUBMIT_COMPONENT_HANDLER(Context ctx, byte[] data, String name, Component.Type type, String description) throws Exception {

        ComponentRepository repository = factory.proxy(name);
        if (repository != null && repository != factory.proxy(type))
            throw new Exception("the component is exists,but type not equals,please delete original component");
        if (repository == null)
            repository = factory.proxy(type);

        if (repository == null)
            throw new Exception("unknown component type");
        repository.save(data, name, description, true);
    }

    @EventMapping
    public List<Component> GET_COMPONENT_LIST_HANDLER(Context ctx,Component.Type type) {
        if (type == null)
            return factory.all();

        ComponentRepository proxy = factory.proxy(type);
        return proxy == null ? null : proxy.all();
    }

    @EventMapping
    public Component GET_COMPONENT_HANDLER(String name) {
        ComponentRepository repository = factory.proxy(name);
        return repository == null ? null : repository.get(name);
    }

    @EventMapping
    public void DELETE_COMPONENT_HANDLER(Context ctx, String name) throws IOException {
        ComponentRepository repository = factory.proxy(name);
        if (repository != null)
            repository.delete(name);
    }


    @EventMapping
    public Class<?> LOAD_COMPONENT_HANDLER(String name,Component.Type type) throws IOException, ClassNotFoundException {
        return factory.proxy(type).loadClass(name);
    }

    protected ComponentCoreFactory componentCoreFactory(){
        return factory;
    }

}
