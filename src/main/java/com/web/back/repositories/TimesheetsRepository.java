package com.web.back.repositories;

import com.web.back.model.entities.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimesheetsRepository extends JpaRepository<Timesheet, UUID> {
    Optional<Timesheet> findByTimesheetIdentifier(String timesheetIdentifier);
}
