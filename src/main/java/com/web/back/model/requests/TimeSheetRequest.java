package com.web.back.model.requests;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record TimeSheetRequest(String timeSheetIdentifier, String description, List<DayOfWeek> daysOfWeek, LocalTime entryTime, LocalTime breakDepartureTime, LocalTime breakReturnTime, LocalTime departureTime) {
}
