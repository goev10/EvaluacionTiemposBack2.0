package com.web.back.services;

import com.web.back.model.dto.UserEmployeesDTO;
import com.web.back.model.entities.UserEmployee;
import com.web.back.repositories.UserEmployeeRepository;
import com.web.back.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserEmployeeService {
    private final UserEmployeeRepository userEmployeeRepository;
    private final UserRepository userRepository;

    public UserEmployeeService(UserEmployeeRepository userEmployeeRepository, UserRepository userRepository) {
        this.userEmployeeRepository = userEmployeeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserEmployeesDTO getEmployeeForUserIdEnriched(Integer userId){
        var user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        var employeeNumbers =  getEmployeeNumbersByUserId(userId);

        return new UserEmployeesDTO(userId, user.get().getUsername(), employeeNumbers);
    }

    @Transactional
    public UserEmployeesDTO upsertUserEmployeeRelations(Integer userId, List<String> employeeNumbers) {
        var user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        var existingEmployeeNumbers = getEmployeeNumbersByUserId(userId);

        var mergedEmployeeNumbers = new HashSet<>(existingEmployeeNumbers);
        mergedEmployeeNumbers.addAll(employeeNumbers);

        var toAdd = mergedEmployeeNumbers.stream()
                .filter(empNum -> !existingEmployeeNumbers.contains(empNum))
                .map(empNum -> new UserEmployee(userId, empNum))
                .toList();

        userEmployeeRepository.saveAll(toAdd);

        var newSetOfEmployeeNumbers = getEmployeeNumbersByUserId(userId);

        return new UserEmployeesDTO(userId, user.get().getUsername(), newSetOfEmployeeNumbers);
    }

    @Transactional
    public void deleteUserEmployeeRelations(Integer userId, String employeeNumber) {
        var user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        userEmployeeRepository.deleteByIdUserIdAndIdEmployeeNumber(userId, employeeNumber);
    }

    private HashSet<String> getEmployeeNumbersByUserId(Integer userId) {
        return userEmployeeRepository.findAll().stream()
                .filter(relation -> relation.getId().getUserId().equals(userId))
                .map(relation -> relation.getId().getEmployeeNumber())
                .collect(Collectors.toCollection(HashSet::new));
    }
}
