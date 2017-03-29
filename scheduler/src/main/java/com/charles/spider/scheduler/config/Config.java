package com.charles.spider.scheduler.config;

import java.util.List;

/**
 * Created by lq on 17-3-29.
 */
public class Config {
    public static final String INIT_MASTER_PORT = "init.master.port";
    public static final String PROCESSER_THREADS_COUNT = "init.processor.threads.count";
    public static final String INIT_LISTEN_PORT = "init.listen.port";

    public static final String CONFIG_RULES_PATH = "config.rules.path";


    private List<Chain> defaultChains = null;
    private List<Timer> defaultTimers = null;
    private List<Proxy> proxies = null;
}
