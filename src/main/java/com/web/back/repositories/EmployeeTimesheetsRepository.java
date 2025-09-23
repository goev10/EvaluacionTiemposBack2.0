package com.web.back.repositories;

import com.web.back.model.entities.EmployeeTimesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeTimesheetsRepository extends JpaRepository<EmployeeTimesheet, UUID> {
    List<EmployeeTimesheet> findAllByEmployeeId(UUID employeeId);
    List<EmployeeTimesheet> findAllByEmployeeNumEmployee(String employeeNumber);
}
