package com.Agile.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserStory {

    private String name;
    private String title;
    private String description;
    private int storyPoints;
    private int businessValue;
    private MoSCoW moscowPriority;
    private int timeCriticality;
    private int riskReduction;

    public UserStory(String name, String title, String description,
                     int storyPoints, int businessValue,
                     MoSCoW moscowPriority, int timeCriticality, int riskReduction) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.storyPoints = storyPoints;
        this.businessValue = businessValue;
        this.moscowPriority = moscowPriority;
        this.timeCriticality = timeCriticality;
        this.riskReduction = riskReduction;
    }

    @Override
    public String toString() {
        return "UserStory{name='" + name + "', title='" + title + "', points=" + storyPoints + "}";
    }
}
