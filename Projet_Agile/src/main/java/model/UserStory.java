package com.Agile.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_stories")
@Getter
@Setter
@NoArgsConstructor
public class UserStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private int storyPoints;

    @Column(nullable = false)
    private int businessValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoSCoW moscowPriority;

    @Column(nullable = false)
    private int timeCriticality;

    @Column(nullable = false)
    private int riskReduction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_backlog_id")
    private ProductBacklog productBacklog;

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