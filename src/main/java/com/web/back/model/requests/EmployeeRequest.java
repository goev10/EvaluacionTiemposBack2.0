package com.web.back.model.requests;

import java.time.Instant;

public record EmployeeRequest(String employeeNumber, String name, String grouper1, String grouper2, String grouper3, String grouper4, String grouper5, Instant startDate) {
}
