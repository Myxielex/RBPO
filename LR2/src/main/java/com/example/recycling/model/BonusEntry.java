package com.example.recycling.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "bonus_entries")
public class BonusEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "batch_id")
    @JsonIgnoreProperties({"user", "point", "recyclingType"})
    private RecyclingBatch batch;

    private double points;

    private String status;

    public BonusEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecyclingBatch getBatch() {
        return batch;
    }

    public void setBatch(RecyclingBatch batch) {
        this.batch = batch;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}