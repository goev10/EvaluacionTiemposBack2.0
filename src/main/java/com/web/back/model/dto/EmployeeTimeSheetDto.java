package com.web.back.model.dto;

import java.time.Instant;

public record EmployeeTimeSheetDto(String id, String employeeId, String employeeNumber, String timesheetId, String timeSheetIdentifier, Instant fromDate, Instant toDate) {
}
