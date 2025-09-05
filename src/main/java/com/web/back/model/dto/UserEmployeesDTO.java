package com.web.back.model.dto;

import java.util.Set;

public record UserEmployeesDTO(Integer userId, String userName, Set<String> employeeNumbers) {
}
