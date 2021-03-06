package com.bh.spider.scheduler.cluster.consistent.operation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Operation {
    byte SNAP=2;
    byte WRITE=1;
    byte READ=0;

    String group() default "operation";


    byte action() default WRITE;


    String data() default "";


    boolean sync() default true;



}
