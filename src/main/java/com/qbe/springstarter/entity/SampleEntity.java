package com.qbe.springstarter.entity;

import com.qbe.springstarter.enums.Status;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "sample_entity", schema = "mydb")
public class SampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column
    private Long stock;

    @Column
    private Double weight;

    @Column
    private Float ratio;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean active;

    @Column
    private Character category;

    @Column
    private LocalDate manufacturedDate;

    @Column
    private LocalTime manufacturedTime;

    @Column
    private LocalDateTime createdAt;

    @Column
    private Instant updatedAt;

    @Column(unique = true)
    private UUID externalId;

    @Column(name = "document", columnDefinition = "BYTEA")
    private byte[] document;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    @Version
    @Column(nullable = false)
    private Long version;
}
