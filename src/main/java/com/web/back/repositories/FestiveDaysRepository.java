package com.web.back.repositories;

import com.web.back.model.entities.FestiveDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FestiveDaysRepository extends JpaRepository<FestiveDay, Long> {
}
