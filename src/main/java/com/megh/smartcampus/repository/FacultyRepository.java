package com.megh.smartcampus.repository;

import com.megh.smartcampus.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    @Query("SELECT f FROM Faculty f LEFT JOIN FETCH f.department LEFT JOIN FETCH f.building WHERE f.isActive = true")
    List<Faculty> findByIsActiveTrue();

    /** Used by TrieService - eagerly loads department to avoid LazyInitializationException */
    @Query("SELECT f FROM Faculty f LEFT JOIN FETCH f.department WHERE f.isActive = true")
    List<Faculty> findAllActiveWithDepartment();

    List<Faculty> findByDepartmentIdAndIsActiveTrue(Long deptId);

    @Query("SELECT f FROM Faculty f LEFT JOIN FETCH f.department LEFT JOIN FETCH f.building " +
           "WHERE f.isActive = true AND (" +
           "LOWER(f.name) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(f.designation) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(f.specialization) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Faculty> search(@Param("q") String q, Pageable pageable);
}
