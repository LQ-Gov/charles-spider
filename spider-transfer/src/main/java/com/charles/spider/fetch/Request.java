package com.charles.spider.fetch;

import java.net.URL;
import java.util.Map;

/**
 * Created by lq on 17-6-3.
 */
public interface Request {



    URL url();

    HttpMethod method();

    /**
     * 请求头设置
     * @return
     */
    Map<String,String> headers();

    /**
     * 向服务器请求附带的参数
     * @return
     */
    Map<String,Object> params();


    /**
     * 框架内消息传递附带的数据
     * @return
     */
    Map<String,Object> extra();


    String[] extractor(String key);

    void extractor(String key, String[] modules);
}