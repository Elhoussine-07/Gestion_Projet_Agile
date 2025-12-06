package com.Agile.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Epic {

    private String name;
    private String title;
    private String description;
    private List<UserStory> userStories = new ArrayList<>();

    public Epic(String name, String title, String description) {
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public void addUserStory(UserStory story) {
        userStories.add(story);
    }
}
