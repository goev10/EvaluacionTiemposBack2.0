package com.web.back.model.requests;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EmployeeTimeSheetRequest(@NotNull String employeeNumber, @NotNull String timeSheetIdentifier, @NotNull Instant fromDate, @NotNull Instant toDate) {
}
