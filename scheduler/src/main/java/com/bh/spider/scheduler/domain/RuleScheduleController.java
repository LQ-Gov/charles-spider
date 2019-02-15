package com.bh.spider.scheduler.domain;

import com.bh.spider.fetch.Request;
import com.bh.spider.rule.Rule;
import com.bh.spider.scheduler.BasicScheduler;
import com.bh.spider.scheduler.job.JobCoreScheduler;
import com.bh.spider.store.base.Store;

public interface RuleScheduleController {

    void execute(JobCoreScheduler jobScheduler) throws Exception;

    void close();


    Rule rule();

    /**
     * 终于到了最终读数据库的产生URL的阶段了
     */
    void blast() throws Exception;


    void joinQueue(Request request);


    static RuleScheduleController build(Rule rule, BasicScheduler scheduler, Store store) {
        return new DefaultRuleScheduleController(scheduler,rule,store);
    }

}
