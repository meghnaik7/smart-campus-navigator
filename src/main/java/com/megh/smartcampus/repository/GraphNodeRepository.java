package com.megh.smartcampus.repository;

import com.megh.smartcampus.entity.GraphNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GraphNodeRepository extends JpaRepository<GraphNode, Long> {
    @Query("SELECT n FROM GraphNode n LEFT JOIN FETCH n.building WHERE n.isActive = true")
    List<GraphNode> findAllActive();

    List<GraphNode> findByBuildingIdAndIsActiveTrue(Long buildingId);
}
