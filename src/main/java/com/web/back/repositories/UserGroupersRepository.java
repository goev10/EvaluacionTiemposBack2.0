package com.web.back.repositories;

import com.web.back.model.entities.UserGrouper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupersRepository extends JpaRepository<UserGrouper, Long> {
    List<UserGrouper> findAllByUserId(int userId);
}
