package com.bh.spider.transfer.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by lq on 17-6-21.
 */
public class Component implements Serializable {
    public enum State {
        NULL, TMP, VALID
    }

    public enum Type {
        UNKNOWN,
        CONFIG,
        COMMON,
        EXTRACTOR,
        SYSTEM
    }

    private long id;
    private String name;
    private String path;
    private String hash;
    private Type type;
    private String detail;
    private Date updateTime;

    private State state = State.NULL;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }


    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}