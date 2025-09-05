package com.web.back.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_employee")
public class UserEmployee {
    @EmbeddedId
    private UserEmployeeId id;

    public UserEmployee() { }

    public UserEmployee(Integer userId, String employeeId) {
        this.id = new UserEmployeeId();
        this.id.setUserId(userId);
        this.id.setEmployeeNumber(employeeId);
    }
}
