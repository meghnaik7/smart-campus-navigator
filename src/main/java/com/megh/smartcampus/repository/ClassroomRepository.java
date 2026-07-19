package com.megh.smartcampus.repository;

import com.megh.smartcampus.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    @Query("SELECT c FROM Classroom c LEFT JOIN FETCH c.building WHERE c.isActive = true")
    List<Classroom> findByIsActiveTrue();

    /** Used by TrieService - eagerly loads building to avoid LazyInitializationException */
    @Query("SELECT c FROM Classroom c LEFT JOIN FETCH c.building WHERE c.isActive = true")
    List<Classroom> findAllActiveWithBuilding();

    List<Classroom> findByBuildingIdAndIsActiveTrue(Long buildingId);
    List<Classroom> findByAvailabilityStatusAndIsActiveTrue(Classroom.AvailabilityStatus status);

    @Query("SELECT c FROM Classroom c LEFT JOIN FETCH c.building WHERE c.isActive = true AND (" +
           "LOWER(c.roomNumber) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Classroom> search(@Param("q") String q, Pageable pageable);
}
