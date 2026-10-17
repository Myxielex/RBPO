package com.example.recycling.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterBatchRequest {

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @Positive
    private Long pointId;

    @NotNull
    @Positive
    private Long recyclingTypeId;

    @Positive
    private double mass;

    @NotNull
    private LocalDate collectionDate;
}
