package com.example.recycling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Entity
@Table(name = "collection_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String address;

    @JsonIgnoreProperties("collectionPoints")
    @ManyToMany
    @JoinTable(
            name = "collection_point_types",
            joinColumns = @JoinColumn(name = "collection_point_id"),
            inverseJoinColumns = @JoinColumn(name = "recycling_type_id")
    )
    private List<RecyclingType> acceptedTypes;

    private boolean availability;

    @Positive
    @Column(nullable = false)
    private double dailyMassLimit;
}
