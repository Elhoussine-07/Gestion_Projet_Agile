package com.Agile.demo.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractWorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;


    @Column(nullable = false, length = 255)
    protected String title;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    protected WorkItemStatus status = WorkItemStatus.TODO;


    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime createdDate;


    @Column(name = "updated_date")
    protected LocalDateTime updatedDate;


    public AbstractWorkItem(String title, WorkItemStatus status) {
        this.title = title;
        this.status = status;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }


    public AbstractWorkItem(String title) {
        this(title, WorkItemStatus.TODO);
    }



    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }


    public void updateStatus(WorkItemStatus newStatus) {
        this.status = newStatus;
        this.updatedDate = LocalDateTime.now();
    }


    public boolean isDone() {
        return this.status == WorkItemStatus.DONE;
    }


    public boolean isInProgress() {
        return this.status == WorkItemStatus.IN_PROGRESS;
    }


    public boolean isBlocked() {
        return this.status == WorkItemStatus.BLOCKED;
    }


    public void start() {
        if (this.status == WorkItemStatus.TODO) {
            updateStatus(WorkItemStatus.IN_PROGRESS);
        }
    }


    public void complete() {
        updateStatus(WorkItemStatus.DONE);
    }


    public void block() {
        updateStatus(WorkItemStatus.BLOCKED);
    }


    public int getProgress() {
        switch (this.status) {
            case TODO:
                return 0;
            case IN_PROGRESS:
            case IN_REVIEW:
            case TESTING:
                return 50;
            case DONE:
                return 100;
            case BLOCKED:
                return 0;
            default:
                return 0;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractWorkItem)) return false;
        AbstractWorkItem that = (AbstractWorkItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title='%s', status=%s}",
                getClass().getSimpleName(), id, title, status);
    }
}