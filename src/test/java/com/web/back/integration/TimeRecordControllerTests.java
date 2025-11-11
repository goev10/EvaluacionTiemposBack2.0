package com.web.back.integration;

import com.web.back.model.entities.Employee;
import com.web.back.model.requests.TimeRecordGeneralRequest;
import com.web.back.repositories.EmployeeRepository;
import com.web.back.repositories.TimeRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TimeRecordControllerTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    static {
        mysql.start();
    }

    @Test
    void bulkTimeRecord_SavesEntry_ASimpleRequestForAnExistentEmployeeIsReceived() {
        var employee = new Employee();
        employee.setNumEmployee("EMP001");
        employee.setName("John Doe");
        employee.setGrouper1("ABC");
        employeeRepository.save(employee);

        List<TimeRecordGeneralRequest> bulkRequest = List.of(
                new TimeRecordGeneralRequest(employee.getNumEmployee(), LocalDate.of(2024, 6, 1), LocalTime.of(9, 0))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<TimeRecordGeneralRequest>> request = new HttpEntity<>(bulkRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/time-record/bulk", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        var timeRecords = timeRecordRepository.findAll();

        assertEquals(1, timeRecords.size());

        var employeeForTimeRecord = employeeRepository.findById(timeRecords.get(0).getEmployeeId());

        assertTrue(employeeForTimeRecord.isPresent());

        assertEquals(employee.getNumEmployee(), employeeForTimeRecord.get().getNumEmployee());
    }
}
