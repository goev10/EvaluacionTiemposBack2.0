package com.web.back.model.requests;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;

public record EmployeeTimeSheetRequest(@NotNull String employeeNumber, @NotNull String timeSheetIdentifier, @NotNull LocalDate fromDate, @NotNull LocalDate toDate) {
}
