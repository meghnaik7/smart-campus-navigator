package com.megh.smartcampus.repository;

import com.megh.smartcampus.entity.RouteHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteHistoryRepository extends JpaRepository<RouteHistory, Long> {
    List<RouteHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT r.destinationName, COUNT(r) as cnt FROM RouteHistory r GROUP BY r.destinationName ORDER BY cnt DESC")
    List<Object[]> findTopDestinations(Pageable pageable);
}
