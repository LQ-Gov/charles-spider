package com.bh.spider.client;

import com.bh.common.WatchFilter;
import com.bh.spider.client.context.ClientFetchContext;
import com.bh.spider.client.converter.TypeConverter;
import com.bh.spider.client.receiver.Receiver;
import com.bh.spider.client.sender.Sender;
import com.bh.spider.fetch.*;
import com.bh.spider.fetch.impl.FetchResponse;
import com.bh.spider.fetch.impl.FinalFetchContext;
import com.bh.spider.fetch.impl.RequestBuilder;
import com.bh.spider.rule.Rule;
import com.bh.spider.transfer.CommandCode;
import com.bh.spider.transfer.Json;
import com.bh.spider.transfer.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Created by lq on 17-3-25.
 */
public class Client {
    private final static Logger logger = LoggerFactory.getLogger(Client.class);


    private String server = null;

    private RuleOperation ruleOperation = null;
    private ComponentOperation componentOperation = null;
    private RequestOperation requestOperation = null;

    private Receiver receiver = null;
    private Sender sender = null;

    private SocketChannel channel;
    private Properties properties = null;


    public Client(String server) {
        this(server, null);
    }


    public Client(String server, Properties properties) {
        this.properties = properties == null ? new Properties() : properties;
        this.server = server;


    }


    public boolean open() throws URISyntaxException, IOException {
        URI uri = new URI("tcp://" + server);

        this.channel = SocketChannel.open(new InetSocketAddress(uri.getHost(),uri.getPort()));

        receiver = new Receiver(this.channel.socket());
        receiver.start();

        this.sender = new Sender(channel, receiver);
        this.ruleOperation = new RuleOperation(this.sender);
        this.componentOperation = new ComponentOperation(this.sender, this.properties);
        this.requestOperation = new RequestOperation(this.sender);
        return true;
    }

    public void close() throws IOException, InterruptedException {
        if (channel != null && channel.isConnected()) channel.close();
        if (receiver.isAlive()) receiver.join();

    }

    public ComponentOperation component() {
        return componentOperation;
    }

    public RuleOperation rule() {
        return ruleOperation;
    }

    public RequestOperation request() {
        return requestOperation;
    }


    @SafeVarargs
    public final Future<FetchResponse> crawler(Request req, Rule rule, Class<? extends Extractor>... extractors) throws MalformedURLException {

        FetchContext base = new ClientFetchContext(req);
        return sender.stream(CommandCode.FETCH, response -> {

            FetchContext ctx = new FinalFetchContext(base, response);
            try {
                for (Class<?> it : extractors) {

                    Extractor extractor = (Extractor) it.newInstance();
                    try {
                        extractor.run(ctx);
                    } catch (ExtractorChainException e) {
                        if (e.result() == Behaviour.TERMINATION) break;

                    } catch (Exception e) {
                        //此处做报告
                        e.printStackTrace();
                    }
                }
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }


        }, new TypeConverter<>(FetchResponse.class), req, rule);
    }

    @SafeVarargs
    public final Future<FetchResponse> crawler(Request req, Class<? extends Extractor>... extractors) throws MalformedURLException {
        return crawler(req, null, extractors);
    }


    @SafeVarargs
    public final Future<FetchResponse> crawler(String url, Class<? extends Extractor>... extractors) throws MalformedURLException {
        return crawler(url, null, extractors);
    }

    @SafeVarargs
    public final Future<FetchResponse> crawler(String url, Rule rule, Class<? extends Extractor>... extractors) throws MalformedURLException {
        return crawler(RequestBuilder.create(url).build(), rule, extractors);
    }

    public <T> void watch(String point, Class<T> valueClass, Consumer<T> consumer) {

        sender.stream(CommandCode.WATCH, consumer, new TypeConverter<>(valueClass), point);
    }


    public void watch(String point, WatchFilter filter, Consumer<String> consumer) {

    }

    public void unwatch(String point) {
        sender.write(CommandCode.UNWATCH, null, point);
    }


    public Map<String, String> profile() {
        Type returnType = Json.mapType(String.class, String.class);
        return sender.write(CommandCode.PROFILE, returnType);
    }


    public List<Node> nodes() {
        ParameterizedType returnType = ParameterizedTypeImpl.make(List.class, new Type[]{Node.class}, null);
        return sender.write(CommandCode.GET_NODE_LIST, returnType);
    }


}
