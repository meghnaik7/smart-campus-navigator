package com.megh.smartcampus.repository;

import com.megh.smartcampus.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByStudentId(String studentId);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.lastName)  LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.email)     LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<User> searchUsers(@Param("q") String q, Pageable pageable);
}
