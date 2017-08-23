package com.charles.spider.fetch;

import java.util.Map;

public interface Response {

    int code();

    Map<String,String> headers();

    byte[] data();
}