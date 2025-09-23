package com.web.back.services;

import com.web.back.model.dto.EmployeeDto;
import com.web.back.model.entities.Employee;
import com.web.back.model.requests.EmployeeRequest;
import com.web.back.repositories.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeDto> getAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EmployeeDto getEmployeeById(String id) {
        return employeeRepository.findById(UUID.fromString(id))
                .map(this::mapToDto)
                .orElse(null);
    }

    public EmployeeDto getEmployeeByEmployeeNumber(String employeeNumber) {
        return employeeRepository.findByNumEmployee(employeeNumber)
                .map(this::mapToDto)
                .orElse(null);
    }

    public List<EmployeeDto> upsertEmployees(List<EmployeeRequest> employees) {
        List<Employee> entities = employees.stream().map(req -> employeeRepository.findByNumEmployee(req.employeeNumber())
                .map(existing -> getEmployee(req, existing))
                .orElseGet(() -> mapToEntity(req))).collect(Collectors.toList());

        List<Employee> saved = employeeRepository.saveAll(entities);
        return saved.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public void deleteEmployeeById(String id) {
        employeeRepository.deleteById(UUID.fromString(id));
    }

    private Employee mapToEntity(EmployeeRequest req) {
        Employee e = new Employee();
        e.setNumEmployee(req.employeeNumber());
        return getEmployee(req, e);
    }

    private Employee getEmployee(EmployeeRequest req, Employee existing) {
        existing.setName(req.name());
        existing.setGrouper1(req.grouper1());
        existing.setGrouper2(req.grouper2());
        existing.setGrouper3(req.grouper3());
        existing.setGrouper4(req.grouper4());
        existing.setGrouper5(req.grouper5());

        if(req.startDate() != null){
            existing.setStartDate(req.startDate());
        }

        return existing;
    }

    private EmployeeDto mapToDto(Employee e) {
        return new EmployeeDto(
                e.getId().toString(),
                e.getNumEmployee(),
                e.getName(),
                e.getGrouper1(),
                e.getGrouper2(),
                e.getGrouper3(),
                e.getGrouper4(),
                e.getGrouper5(),
                e.getStartDate()
        );
    }
}
