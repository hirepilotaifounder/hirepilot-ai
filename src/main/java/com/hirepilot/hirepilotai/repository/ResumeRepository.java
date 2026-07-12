package com.hirepilot.hirepilotai.repository;

import com.hirepilot.hirepilotai.entity.Resume;
import com.hirepilot.hirepilotai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserOrderByActiveDescUploadedAtDesc(User user);

    Optional<Resume> findByIdAndUser(Long id, User user);

    @Modifying
    @Query("""
    UPDATE Resume r
    SET r.active = false
    WHERE r.user = :user
""")
    void deactivateAllByUser(@Param("user") User user);
}