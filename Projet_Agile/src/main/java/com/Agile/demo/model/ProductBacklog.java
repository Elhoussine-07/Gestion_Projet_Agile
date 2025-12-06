package com.Agile.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_backlogs")
@Getter
@Setter
@NoArgsConstructor
public class ProductBacklog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "productBacklog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStory> stories = new ArrayList<>();

    public ProductBacklog(String name) {
        this.name = name;
    }

    public void addStory(UserStory story) {
        stories.add(story);
        story.setProductBacklog(this);
    }

    public void removeStory(UserStory story) {
        stories.remove(story);
        story.setProductBacklog(null);
    }
}