package com.web.back.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Size(max = 11)
    @Column(name = "num_employee", length = 11)
    private String numEmployee;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @Size(max = 50)
    @Column(name = "grouper_1", length = 50)
    private String grouper1;

    @Size(max = 50)
    @Column(name = "grouper_2", length = 50)
    private String grouper2;

    @Size(max = 50)
    @Column(name = "grouper_3", length = 50)
    private String grouper3;

    @Size(max = 50)
    @Column(name = "grouper_4", length = 50)
    private String grouper4;

    @Size(max = 50)
    @Column(name = "grouper_5", length = 50)
    private String grouper5;

    @Column(name = "start_date")
    private Instant startDate;
}
