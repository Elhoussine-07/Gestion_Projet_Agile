package com.agile.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "epics")
@Getter
@Setter
@NoArgsConstructor
public class Epic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "epic", cascade = CascadeType.ALL)
    private List<UserStory> userStories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_backlog_id")
    private ProductBacklog productBacklog;

    public Epic(String name, String title, String description) {
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public void addUserStory(UserStory story) {
        userStories.add(story);
        story.setEpic(this);
    }

    public void removeUserStory(UserStory story) {
        userStories.remove(story);
        story.setEpic(null);
    }
}