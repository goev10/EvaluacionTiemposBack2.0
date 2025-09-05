package com.web.back.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class UserEmployeeId implements Serializable {
    @Serial
    private static final long serialVersionUID = -7146757627229428079L;
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Size(max = 20)
    @NotNull
    @Column(name = "employee_number", nullable = false, length = 20)
    private String employeeNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserEmployeeId entity = (UserEmployeeId) o;
        return Objects.equals(this.userId, entity.userId) &&
                Objects.equals(this.employeeNumber, entity.employeeNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, employeeNumber);
    }

}
