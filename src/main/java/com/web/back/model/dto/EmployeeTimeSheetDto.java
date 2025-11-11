package com.web.back.model.dto;

import java.time.Instant;
import java.time.LocalDate;

public record EmployeeTimeSheetDto(String id, String employeeId, String employeeNumber, String timesheetId, String timeSheetIdentifier, LocalDate fromDate, LocalDate toDate) {
}
