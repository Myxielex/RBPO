package com.example.recycling.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.recycling.repository.RecyclingBatchRepository;

@Entity
@Table(name = "collection_points")
public class CollectionPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    private double dailyMassLimit;

    public CollectionPoint() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<RecyclingType> getAcceptedTypes() {
        return acceptedTypes;
    }

    public void setAcceptedTypes(List<RecyclingType> acceptedTypes) {
        this.acceptedTypes = acceptedTypes;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public double getDailyMassLimit() {
        return dailyMassLimit;
    }

    public void setDailyMassLimit(double dailyMassLimit) {
        this.dailyMassLimit = dailyMassLimit;
    }
}