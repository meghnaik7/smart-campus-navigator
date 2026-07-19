package com.megh.smartcampus.repository;

import com.megh.smartcampus.entity.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    Optional<Building> findByCode(String code);
    List<Building> findByIsActiveTrue();
    List<Building> findByTypeAndIsActiveTrue(Building.BuildingType type);
    boolean existsByCode(String code);

    @Query("SELECT b FROM Building b WHERE b.isActive = true AND (" +
           "LOWER(b.name) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(b.code) LIKE LOWER(CONCAT('%',:kw,'%')))")
    Page<Building> search(@Param("kw") String kw, Pageable pageable);
}
