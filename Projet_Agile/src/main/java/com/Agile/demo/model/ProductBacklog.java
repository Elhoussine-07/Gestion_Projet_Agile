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

    @Column(name = "total_business_value")
    private Integer totalBusinessValue = 0;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "productBacklog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStory> stories = new ArrayList<>();

    @OneToMany(mappedBy = "productBacklog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Epic> epics = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "prioritization_method")
    private PrioritizationMethod selectedMethod = PrioritizationMethod.MOSCOW;

    @Transient
    private IPrioritizationStrategy prioritizationStrategy;

    public void applyPriorities(List<UserStory> sortedStories) {
        for (int i = 0; i < sortedStories.size(); i++) {
            sortedStories.get(i).setPriority(i + 1);
        }
    }

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