package com.web.back.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_groupers")
public class UserGrouper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

}
