package com.charles.spider.scheduler.job;

import com.charles.spider.common.command.Commands;
import com.charles.spider.common.extractor.Extractor;
import com.charles.spider.common.http.Request;
import com.charles.spider.scheduler.BasicScheduler;
import com.charles.spider.scheduler.Command;
import com.charles.spider.scheduler.rule.RuleDecorator;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map;
import java.util.Queue;

/**
 * Created by lq on 17-6-13.
 */
public class RuleExecuteObject implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        BasicScheduler scheduler = (BasicScheduler) context.getMergedJobDataMap().get("basic-scheduler");
        RuleDecorator rule = (RuleDecorator) context.getMergedJobDataMap().get("rule-decorator");


        Queue<Request> queue = rule.getRequests();

        Request req = queue.poll();

        if (req != null) {
            bind(req, rule.extractors());
            Command cmd = new Command(Commands.FETCH, null, new Object[]{req});
            scheduler.process(cmd);
        }
    }

    private void bind(Request req, Map<String, String[]> extractors) {
        if (extractors == null || extractors.isEmpty()) return;
        extractors.forEach((k, v) -> {
            if (req.extractor(k) == null) req.extractor(k, v);
        });
    }
}
