package com.example.recycling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "recycling_batches")
public class RecyclingBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties
    private User user;

    @ManyToOne
    @JoinColumn(name = "point_id")
    @JsonIgnoreProperties
    private CollectionPoint point;

    @ManyToOne
    @JoinColumn(name = "recycling_type_id")
    @JsonIgnoreProperties("collectionPoints")
    private RecyclingType recyclingType;

    private double mass;

    private LocalDate collectionDate;

    private String status;

    public RecyclingBatch() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CollectionPoint getPoint() {
        return point;
    }

    public void setPoint(CollectionPoint point) {
        this.point = point;
    }

    public RecyclingType getRecyclingType() {
        return recyclingType;
    }

    public void setRecyclingType(RecyclingType recyclingType) {
        this.recyclingType = recyclingType;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDate collectionDate) {
        this.collectionDate = collectionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}