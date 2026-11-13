package com.example.recycling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "bonus_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BonusEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "batch_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"user", "point", "recyclingType"})
    private RecyclingBatch batch;

    @PositiveOrZero
    @Column(nullable = false)
    private double points;

    @Column(nullable = false)
    private String status;
}
