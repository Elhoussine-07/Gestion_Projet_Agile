package com.Agile.demo.model;

import lombok.*;
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
@Builder
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

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SprintBacklog> sprints = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> members = new ArrayList<>();

    // Créer automatiquement le productBacklog
    @PrePersist
    protected void onCreate() {
        if (this.productBacklog == null) {
            this.productBacklog = new ProductBacklog();
            this.productBacklog.setName(this.name + " - Product Backlog");
            this.productBacklog.setProject(this);
        }
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