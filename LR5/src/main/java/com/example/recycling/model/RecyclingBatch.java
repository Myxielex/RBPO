package com.example.recycling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Entity
@Table(name = "recycling_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecyclingBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "point_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CollectionPoint point;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recycling_type_id", nullable = false)
    @JsonIgnoreProperties("collectionPoints")
    private RecyclingType recyclingType;

    @Positive
    @Column(nullable = false)
    private double mass;

    @NotNull
    @Column(nullable = false)
    private LocalDate collectionDate;

    @Column(nullable = false)
    private String status;
}
