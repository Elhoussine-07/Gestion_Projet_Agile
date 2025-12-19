package com.Agile.demo.planning.repository;

import com.Agile.demo.model.ProductBacklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductBacklogRepository extends JpaRepository<ProductBacklog, Long> {

    Optional<ProductBacklog> findByProjectId(Long projectId);

    @Query("SELECT pb FROM ProductBacklog pb LEFT JOIN FETCH pb.stories WHERE pb.id = :id")
    Optional<ProductBacklog> findByIdWithStories(Long id);
}