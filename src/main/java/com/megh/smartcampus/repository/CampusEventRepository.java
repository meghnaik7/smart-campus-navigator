package com.megh.smartcampus.repository;

import com.megh.smartcampus.entity.CampusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampusEventRepository extends JpaRepository<CampusEvent, Long> {
    List<CampusEvent> findByIsActiveTrue();

    @Query("SELECT e FROM CampusEvent e WHERE e.isActive = true AND e.startTime >= :now ORDER BY e.startTime ASC")
    List<CampusEvent> findUpcoming(@Param("now") LocalDateTime now);
}
