package com.example.recycling.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;
import com.example.recycling.model.CollectionPoint;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "recycling_types")
public class RecyclingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String unit;

    private double pointsPerKg;

    @JsonIgnore
    @ManyToMany(mappedBy = "acceptedTypes")
    private List<CollectionPoint> collectionPoints;

    public RecyclingType() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPointsPerKg() {
        return pointsPerKg;
    }

    public void setPointsPerKg(double pointsPerKg) {
        this.pointsPerKg = pointsPerKg;
    }

    public List<CollectionPoint> getCollectionPoints() {
        return collectionPoints;
    }

    public void setCollectionPoints(List<CollectionPoint> collectionPoints) {
        this.collectionPoints = collectionPoints;
    }
}