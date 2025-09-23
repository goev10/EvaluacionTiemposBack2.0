package com.web.back.repositories;

import com.web.back.model.entities.GroupersConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupersConfigurationsRepository extends JpaRepository<GroupersConfiguration, Long> {
}
