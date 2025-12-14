package com.agile.demo.planning.repository;

import com.agile.demo.model.Epic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Long> {

    List<Epic> findByProductBacklogId(Long productBacklogId);

    boolean existsByTitleAndProductBacklogId(String title, Long productBacklogId);

    @Query("SELECT e FROM Epic e LEFT JOIN FETCH e.userStories WHERE e.id = :id")
    Optional<Epic> findByIdWithUserStories(Long id);
}