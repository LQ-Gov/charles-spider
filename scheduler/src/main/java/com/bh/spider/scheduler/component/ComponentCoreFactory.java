package com.bh.spider.scheduler.component;

import com.bh.spider.fetch.Extractor;
import com.bh.spider.scheduler.config.Config;
import com.bh.spider.store.service.Service;
import com.bh.spider.transfer.entity.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by lq on 17-3-16.
 */
public class ComponentCoreFactory {
    private static final Logger logger = LoggerFactory.getLogger(ComponentCoreFactory.class);

    private Service<Component> service;

    private GlobalComponentProxy globalProxy;
    private CommonComponentProxy commonProxy;
    private ExtractorComponentProxy extractorProxy;


    public ComponentCoreFactory(Service<Component> service) throws IOException {

        this.service = service;

        commonProxy = new CommonComponentProxy(service, Paths.get(Config.INIT_DATA_PATH, "common"));

        extractorProxy = new ExtractorComponentProxy(commonProxy, service, Paths.get(Config.INIT_DATA_PATH, "extractor"));

        globalProxy = new GlobalComponentProxy(this, service);


    }


    public ComponentProxy proxy(Component.Type type) {
        switch (type) {
            case COMMON:
                return commonProxy;
            case EXTRACTOR:
                return extractorProxy;
            default:
                return null;
        }
    }


    public ComponentProxy proxy() {
        return globalProxy;
    }


    public Extractor extractorComponent(String componentName) throws IOException, ComponentBuildException {

        return extractorProxy.component(componentName);
    }
}