package com.web.back.model.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record TimeSheetDto(String id, String timeSheetIdentifier, String description, List<DayOfWeek> daysOfWeek, LocalTime entryTime, LocalTime breakDepartureTime, LocalTime breakReturnTime, LocalTime departureTime) {
}
