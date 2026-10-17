package com.example.recycling.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Entity
@Table(name = "recycling_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecyclingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String unit;

    @Positive
    @Column(nullable = false)
    private double pointsPerKg;

    @JsonIgnore
    @ManyToMany(mappedBy = "acceptedTypes")
    private List<CollectionPoint> collectionPoints;
}
