package com.Agile.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "epics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private List<UserStory> userStories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_backlog_id")
    private ProductBacklog productBacklog;

    // Constructeur avec Builder est géré par Lombok via l'annotation @Builder

    public void addUserStory(UserStory story) {
        userStories.add(story);
        story.setEpic(this);
    }

    public void removeUserStory(UserStory story) {
        userStories.remove(story);
        story.setEpic(null);
    }
}
