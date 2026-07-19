package com.megh.smartcampus.repository;

import com.megh.smartcampus.entity.GraphEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GraphEdgeRepository extends JpaRepository<GraphEdge, Long> {
    @Query("SELECT e FROM GraphEdge e JOIN FETCH e.sourceNode JOIN FETCH e.targetNode WHERE e.isActive = true")
    List<GraphEdge> findAllActiveWithNodes();
}
