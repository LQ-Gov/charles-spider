package com.bh.spider.scheduler.cluster;

import com.bh.common.utils.CommandCode;
import com.bh.spider.common.component.Component;
import com.bh.spider.scheduler.BasicSchedulerComponentAssistant;
import com.bh.spider.scheduler.Config;
import com.bh.spider.scheduler.cluster.consistent.operation.Entry;
import com.bh.spider.scheduler.cluster.consistent.operation.Operation;
import com.bh.spider.scheduler.cluster.consistent.operation.OperationRecorder;
import com.bh.spider.scheduler.component.ComponentRepository;
import com.bh.spider.scheduler.context.Context;
import com.bh.spider.scheduler.event.Command;
import com.bh.spider.scheduler.event.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class ClusterSchedulerComponentAssistant extends BasicSchedulerComponentAssistant {

    private final static Logger logger = LoggerFactory.getLogger(ClusterSchedulerComponentAssistant.class);

    private ClusterScheduler scheduler;

    private OperationRecorder componentOperationRecorder;

    public ClusterSchedulerComponentAssistant(Config cfg, ClusterScheduler scheduler) throws IOException {
        super(cfg, scheduler);
        this.scheduler = scheduler;

        this.componentOperationRecorder = OperationRecorder
                .builder("component")
                .snapshot(() -> componentCoreFactory().snapshot(), 2000)
                .dir(Paths.get(cfg.get(Config.INIT_OPERATION_LOG_PATH)))
                .build();

//        this.componentOperationRecorder = OperationRecorderFactory.get("component");

    }


    @CommandHandler
    public void WORKER_GET_COMPONENT_HANDLER(Context ctx, String name) throws IOException, CloneNotSupportedException {

        ComponentRepository repository = componentCoreFactory().proxy(name);

        Component component = repository.get(name, true);
//
        Command cmd = new Command(ctx, CommandCode.SUBMIT_COMPONENT.name(), component);

        ctx.write(cmd);
        logger.info("WORKER_GET_COMPONENT_HANDLER 执行");
    }

    @CommandHandler
    public void CHECK_COMPONENT_OPERATION_COMMITTED_INDEX_HANDLER(Context ctx, long committedIndex) throws IOException {
        long localCommittedIndex = componentOperationRecorder.committedIndex();
        if (localCommittedIndex > committedIndex) {
            List<Entry> entries = componentOperationRecorder.load(committedIndex + 1, localCommittedIndex);
            if (!entries.isEmpty()) {
                Command cmd = new Command(ctx, CommandCode.WRITE_OPERATION_ENTRIES.name(), entries);
                ctx.write(cmd);
            }
        }
    }


    @Override
    @CommandHandler(autoComplete = false)
    @Operation(group = "component", data = "ADD ${name} ${type}")
    public void SUBMIT_COMPONENT_HANDLER(Context ctx, byte[] data, String name, Component.Type type, String description) throws Exception {
        super.SUBMIT_COMPONENT_HANDLER(ctx, data, name, type, description);
        ctx.commandCompleted(null);

    }

    @Override
    @CommandHandler(autoComplete = false)
    @Operation(group = "component", data = "DELETE ${name}")
    public void DELETE_COMPONENT_HANDLER(Context ctx, String name) throws IOException {
        super.DELETE_COMPONENT_HANDLER(ctx, name);
        ctx.commandCompleted(null);
    }


}
