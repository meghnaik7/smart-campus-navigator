package com.megh.smartcampus.repository;

import com.megh.smartcampus.entity.SearchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT s.query, COUNT(s) as cnt FROM SearchHistory s GROUP BY s.query ORDER BY cnt DESC")
    List<Object[]> findTopQueries(Pageable pageable);
}
