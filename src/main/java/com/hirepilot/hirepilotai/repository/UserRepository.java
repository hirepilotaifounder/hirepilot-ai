package com.hirepilot.hirepilotai.repository;

import com.hirepilot.hirepilotai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}