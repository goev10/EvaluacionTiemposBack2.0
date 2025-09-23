package com.web.back.model.requests;

import java.time.Instant;

public record EmployeeTimeSheetRequest(String employeeNumber, String timeSheetIdentifier, Instant fromDate, Instant toDate) {
}
