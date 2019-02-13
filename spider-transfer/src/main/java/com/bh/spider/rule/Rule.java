package com.bh.spider.rule;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.*;

/**
 * Created by lq on 17-6-7.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS,include = JsonTypeInfo.As.EXISTING_PROPERTY,property = "_class")
public class Rule implements Serializable {

    private Class<?> _class = Rule.class;

    private long id;

    private List<ExtractQueue> extractors = new LinkedList<>();
    /**
     * 定时器
     */
    private String cron;
    /**
     * URL匹配规则
     */
    private String pattern;

    private int parallelCount;
    /**
     * 对此规则的描述
     */
    private String description;


    private String[] policy;

    private boolean valid;

    public Rule() {
    }

    public Rule(long id) {
        this(id, null, null);
    }

    public Rule(String pattern, String cron) {
        this(0, pattern, cron);
    }

    public Rule(long id, String pattern, String cron) {
        this.id = id;
        this.pattern = pattern;
        this.cron = cron;
        this.valid = true;
    }


    public Rule(long id, Rule rule) {
        this.id = id;
        this.extractors = rule.extractors;
        this.cron = rule.cron;
        this.pattern = rule.pattern;
        this.parallelCount = rule.parallelCount;
        this.description = rule.description;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }


    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return obj instanceof Rule && (this == obj || this.getId() == ((Rule) obj).getId());
    }

    public int getParallelCount() {
        return parallelCount;
    }

    public void setParallelCount(int parallelCount) {
        this.parallelCount = parallelCount;
    }

    public List<ExtractQueue> getExtractors() {
        return extractors;
    }

    public void setExtractors(List<ExtractQueue> extractors) {
        this.extractors = extractors;
    }

    public String[] getPolicy() {
        return policy;
    }

    public void setPolicy(String[] policy) {
        this.policy = policy;
    }
}
