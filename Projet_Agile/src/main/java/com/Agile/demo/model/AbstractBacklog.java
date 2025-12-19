package com.Agile.demo.model;

import com.Agile.demo.model.AbstractWorkItem;
import jakarta.persistence.MappedSuperclass;

import java.util.List;

@MappedSuperclass
public abstract class AbstractBacklog {

    protected String name;

    public AbstractBacklog() {}

    public AbstractBacklog(String name) {
        this.name = name;
    }

    public AbstractBacklog(String name, String s) {
    }

    public abstract void addItem(AbstractWorkItem item);

    public abstract void removeItem(AbstractWorkItem item);

    public abstract List<? extends AbstractWorkItem> getItems();

    public String getName() {
        return name;
    }
}
