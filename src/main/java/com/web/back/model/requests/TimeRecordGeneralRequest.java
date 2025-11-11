package com.web.back.model.requests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record TimeRecordGeneralRequest(String employeeNumber, LocalDate date, LocalTime time) {
    public LocalDateTime markDateTime() {
        return LocalDateTime.of(date, time);
    }
}
