package com.web.back.repositories;

import com.web.back.model.entities.UserEmployee;
import com.web.back.model.entities.UserEmployeeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEmployeeRepository extends JpaRepository<UserEmployee, UserEmployeeId> {
    void deleteByIdUserIdAndIdEmployeeNumber(Integer userId, String employeeNumber);
}
