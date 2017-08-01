package com.charles.spider.scheduler.fetcher;

import com.charles.spider.common.extractor.Document;
import com.charles.spider.common.http.FetchContext;
import com.charles.spider.common.http.Request;
import org.apache.http.HttpResponse;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

public class FinalFetchContext implements FetchContext {

    private FetchContext parent;
    private HttpResponse response;
    private int statusCode = 200;

    public FinalFetchContext(FetchContext parent, HttpResponse response) {
        this.parent = parent;
        this.response = response;
        this.statusCode = response.getStatusLine().getStatusCode();
    }

    @Override
    public URL url() {
        return null;
    }

    @Override
    public Request request() {
        return parent.request();
    }

    @Override
    public void response() {

    }

    @Override
    public Document document() {
        return null;
    }

    @Override
    public Document document(Charset charset) {
        return null;
    }

    @Override
    public void set(String key, Object value) {

    }

    @Override
    public void set(Map<String, Object> collection) {

    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public void scheduler(FetchContext ctx, Request req, boolean local) {

    }

    @Override
    public void scheduler(FetchContext ctx, Request req) {

    }

    @Override
    public void scheduler(Request req) {

    }

    @Override
    public void scheduler(FetchContext ctx, String url, boolean local) {

    }

    @Override
    public void scheduler(FetchContext ctx, String url) {

    }

    @Override
    public void scheduler(String url) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public int status() {
        return this.statusCode;
    }
}