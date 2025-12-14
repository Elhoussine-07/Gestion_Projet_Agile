package com.agile.demo.model;

import java.util.List;

public abstract class AbstractBacklog {
    public AbstractBacklog(String name, String s) {
    }

    public abstract void addItem(AbstractWorkItem item);

    public abstract List<AbstractWorkItem> getItems();
}
