package com.bh.spider.scheduler.watch.point;


import com.bh.common.WatchPointKeys;
import com.bh.common.utils.TimeWindow;
import com.bh.spider.common.component.Component;
import com.bh.spider.common.rule.Rule;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Points {
    private final static Map<String, Point> POINTS = new ConcurrentHashMap<>();


    public static <T> Point<T> of(String key) {

        if (StringUtils.isBlank(key)) return null;


        Point point = POINTS.get(key);


        if (point != null) {
            if (point instanceof WeakReferencePoint)
                point = ((WeakReferencePoint) point).original();


            if (point != null) return point;
        }


        synchronized (POINTS) {
            point = POINTS.get(key);

            if (point != null && point.stable()) return point;


            int colonIndex = key.indexOf(":");

            if (colonIndex > 0) {
                point = POINTS.get(key.substring(0, colonIndex));
                if (point != null) {
                    point = point.createChildPoint(key);
                    POINTS.put(point.key(), new WeakReferencePoint(point));
                }

            }
        }


        return point;
    }


    public static List<String> allKeys() {
        return Collections.unmodifiableList(new ArrayList<>(POINTS.keySet()));
    }


    public static void autoClean(String... keys) {

        for (String key : keys) {
            final Point point = POINTS.get(key);
            if (point != null && !point.stable()) {
                synchronized (point) {
                    if (!point.stable())
                        POINTS.remove(key);
                }
            }
        }
    }


    private static void register(Point point) {

        POINTS.put(point.key(), point);
    }

    static {
        register(new TimeWindowPoint<Long>(WatchPointKeys.EVENT_LOOP_DAY_COUNT,new TimeWindow<>(7,1, TimeUnit.DAYS)));
        register(new TimeWindowPoint<Long>(WatchPointKeys.EVENT_LOOP_SECOND_COUNT));
        register(new ValuePoint<Long>(WatchPointKeys.EVENT_LOOP_TOTAL_COUNT));

        register(new TimeWindowPoint<Long>(WatchPointKeys.URL_TOTAL_COUNT));


        register(new ValuePoint<Rule>(WatchPointKeys.SUBMIT_RULE));
        register(new ValuePoint<Component>(WatchPointKeys.SUBMIT_COMPONENT));
        register(new TimeWindowPoint<>(WatchPointKeys.REQUEST_FREQUENCY));



    }

}
