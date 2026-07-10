package com.hirepilot.hirepilotai.repository;

import com.hirepilot.hirepilotai.entity.Resume;
import com.hirepilot.hirepilotai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUser(User user);

}