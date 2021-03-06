package com.bh.spider.store.base;

import com.bh.spider.common.fetch.Request;

import java.util.List;

public interface StoreAccessor {


    boolean insert(Request request, long ruleId);

    boolean save(Request request,long ruleId);

    void update(long ruleId,Long[] reIds,Request.State state);

    void update(long id,Integer code,Request.State state,String message);


    void reset(long ruleId);

    List<Request> find(long ruleId, Request.State state, long size);

    List<Request> find(long ruleId,Request.State state,long offset, long size);


    long count(Long ruleId, Request.State state);



}
