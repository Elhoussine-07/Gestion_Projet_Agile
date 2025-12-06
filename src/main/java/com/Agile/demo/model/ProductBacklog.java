package com.Agile.demo.model;

import java.util.ArrayList;
import java.util.List;

public class ProductBacklog {

    private List<UserStory> stories = new ArrayList<>();

    public void addStory(UserStory story) {
        stories.add(story);
    }

    public List<UserStory> getStories() {
        return stories;
    }
}
