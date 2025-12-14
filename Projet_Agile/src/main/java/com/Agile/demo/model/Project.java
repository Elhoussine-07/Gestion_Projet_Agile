package com.agile.demo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_backlog_id")
    private ProductBacklog productBacklog;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SprintBacklog> sprints = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();

    // Constructeur personnalisé sans l'ID (généré automatiquement)
    public Project(String name, String description, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.productBacklog = new ProductBacklog();
        this.sprints = new ArrayList<>();
        this.members = new ArrayList<>();
    }

    // Méthodes utilitaires pour maintenir la cohérence des relations
    public void addSprint(SprintBacklog sprint) {
        sprints.add(sprint);
        sprint.setProject(this);
    }

    public void removeSprint(SprintBacklog sprint) {
        sprints.remove(sprint);
        sprint.setProject(null);
    }

    public void addMember(User user) {
        members.add(user);
        user.getProjects().add(this);
    }

    public void removeMember(User user) {
        members.remove(user);
        user.getProjects().remove(this);
    }
}