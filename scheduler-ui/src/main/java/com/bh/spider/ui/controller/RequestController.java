package com.bh.spider.ui.controller;

import com.bh.spider.client.Client;
import com.bh.spider.fetch.Request;
import com.bh.spider.fetch.impl.FetchState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

@RestController("/url")
public class RequestController {

    @Autowired
    private Client client;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<Request> list(String ruleId, FetchState state, Date startDate, Date endDate, int skip, int size) {
//        Query query = new Query();
//        if (ruleId != null)
//            query.addCondition(Condition.where("ruleId").is(ruleId));
//
//        Condition condition = Condition.where("updateTime");
//        if (startDate != null)
//            condition = condition.gte(startDate);
//        if (endDate != null)
//            condition = condition.lt(endDate);
//
//        if (condition.isValid())
//            query.addCondition(condition);
//
//        if (state != null)
//            query.addCondition(Condition.where("state").is(state));
//
//        query.skip(skip).limit(size);
//        return client.request().select(query);

        return null;
    }


    @PostMapping
    public void submit(String url) throws MalformedURLException {
        client.request().submit(url);
    }
}
