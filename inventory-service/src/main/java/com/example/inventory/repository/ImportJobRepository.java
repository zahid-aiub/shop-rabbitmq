package com.example.inventory.repository;

import com.example.inventory.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, String> {

    @Modifying
    @Transactional
    @Query("UPDATE ImportJob j SET j.processedRows = j.processedRows + :processed, j.failedRows = j.failedRows + :failed WHERE j.id = :id")
    void updateProgress(@Param("id") String id, @Param("processed") int processed, @Param("failed") int failed);
}
