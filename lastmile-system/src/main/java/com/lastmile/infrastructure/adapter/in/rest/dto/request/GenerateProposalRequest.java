package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class GenerateProposalRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;
}