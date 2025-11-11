package com.web.back.repositories;

import com.web.back.model.entities.TimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeRecordRepository extends JpaRepository<TimeRecord, UUID> {
    List<TimeRecord> findAllByDateBetween(LocalDate dateAfter, LocalDate dateBefore);
    List<TimeRecord> findAllByDateBetweenAndEmployee_IdIn(LocalDate dateAfter, LocalDate dateBefore, List<UUID> employeeIds);
    TimeRecord findByDateAndEmployee_NumEmployeeAndTurn(LocalDate date, String numEmployee, int turn);
    List<TimeRecord> findAllByDateAndEmployee_NumEmployee(LocalDate date, String numEmployee);
}
