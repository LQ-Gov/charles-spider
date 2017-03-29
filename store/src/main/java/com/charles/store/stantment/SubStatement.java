package com.charles.store.stantment;

import com.charles.store.filter.AndFilter;
import com.charles.store.filter.Filter;
import com.charles.store.filter.OrFilter;

/**
 * Created by lq on 17-3-25.
 */
public class SubStatement {
    private Statement parent = null;

    public SubStatement(Statement parent) {
        this.parent = parent;
    }

    public SubStatement and(Filter filter) {
        parent.where(new AndFilter(filter));
        return this;
    }

    public SubStatement or(Filter filter) {
        parent.where(new OrFilter(filter));
        return this;
    }

    public <T> T exec() {
        return parent.exec();
    }
}
