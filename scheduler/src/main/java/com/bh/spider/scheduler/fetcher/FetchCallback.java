package com.bh.spider.scheduler.fetcher;

import com.bh.spider.fetch.Extractor;
import com.bh.spider.fetch.FetchContext;
import com.bh.spider.fetch.Request;
import com.bh.spider.fetch.impl.FetchRequest;
import com.bh.spider.fetch.impl.FetchResponse;
import com.bh.spider.fetch.impl.FetchState;
import com.bh.spider.fetch.impl.FinalFetchContext;
import com.bh.spider.scheduler.BasicScheduler;
import com.bh.spider.scheduler.Command;
import com.bh.spider.scheduler.context.Context;
import com.bh.spider.transfer.CommandCode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.concurrent.FutureCallback;

/**
 * Created by lq on 17-3-18.
 */
public class FetchCallback implements FutureCallback<FetchResponse> {

    private Fetcher fetcher = null;
    private Context trackContext;
    private FetchContext context;
    private BasicScheduler scheduler;

    public FetchCallback(Context trackContext, BasicScheduler scheduler, Fetcher fetcher, FetchContext context) {
        this.trackContext = trackContext;
        this.scheduler = scheduler;
        this.fetcher = fetcher;

        this.context = context;


    }

    @Override
    public void completed(FetchResponse response) {
        this.fetcher.service().execute(() -> {
            FetchContext ctx = new FinalFetchContext(this.context, response);

            int code = ctx.response().code();

            this.trackContext.write(ctx.response());


            Request req = ctx.request();

            String[] chain = req.extractor(String.valueOf(code));

            boolean res = ArrayUtils.isEmpty(chain) ?
                    process(ctx, req.extractor(String.valueOf("default"))) :
                    process(ctx, chain);

            if (res) {
                FetchRequest fr = (FetchRequest) ctx.request();
                fr.setState(FetchState.FINISHED);
                Command cmd = new Command(CommandCode.REPORT, this.trackContext, new Object[]{ctx.request()});
                this.scheduler.process(cmd);
            }

            this.trackContext.complete();
        });

    }

    @Override
    public void failed(Exception e) {
        trackContext.exception(e);
        trackContext.complete();
    }

    @Override
    public void cancelled() {

    }

    private boolean process(FetchContext ctx, String... chain) {
        //String[] chain = req.extractor(String.valueOf(code));

        FetchRequest fr = (FetchRequest) ctx.request();
        if (ArrayUtils.isNotEmpty(chain)) {
            try {
                for (String it : chain) {

                    Extractor extractor = scheduler.extractorComponent(it);


                    if (!extractor.run(ctx)) break;
                }


            } catch (Exception e) {
                //向master报告
                fr.setState(FetchState.EXCEPTION);
                fr.setMessage(e.getMessage());
                return false;
            }
        }
        return true;
    }
}
