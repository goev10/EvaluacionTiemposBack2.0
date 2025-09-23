package com.web.back.services;

import com.web.back.model.dto.GrouperDto;
import com.web.back.model.entities.GroupersConfiguration;
import com.web.back.model.requests.GrouperRequest;
import com.web.back.repositories.GroupersConfigurationsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupersService {
    private final GroupersConfigurationsRepository groupersConfigurationsRepository;

    public GroupersService(GroupersConfigurationsRepository groupersConfigurationsRepository) {
        this.groupersConfigurationsRepository = groupersConfigurationsRepository;
    }

    public List<GrouperDto> getAll() {
        return groupersConfigurationsRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public GrouperDto getById(long id) {
        GroupersConfiguration grouper = groupersConfigurationsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grouper not found"));
        return toDto(grouper);
    }

    public GrouperDto add(GrouperRequest request) {
        GroupersConfiguration grouper = fromRequest(request);
        GroupersConfiguration saved = groupersConfigurationsRepository.save(grouper);
        return toDto(saved);
    }

    public GrouperDto update(long id, GrouperRequest request) {
        GroupersConfiguration grouper = groupersConfigurationsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grouper not found"));
        updateEntity(grouper, request);
        GroupersConfiguration saved = groupersConfigurationsRepository.save(grouper);
        return toDto(saved);
    }

    public void deleteById(long id) {
        groupersConfigurationsRepository.deleteById(id);
    }

    private GrouperDto toDto(GroupersConfiguration entity) {
        return new GrouperDto(
                entity.getId(),
                entity.getName(),
                entity.getShortName(),
                entity.getVisible()
        );
    }

    private GroupersConfiguration fromRequest(GrouperRequest request) {
        GroupersConfiguration entity = new GroupersConfiguration();
        entity.setName(request.name());
        entity.setShortName(request.shortName());
        entity.setVisible(request.isVisible());

        return entity;
    }

    private void updateEntity(GroupersConfiguration entity, GrouperRequest request) {
        entity.setName(request.name());
        entity.setShortName(request.shortName());
        entity.setVisible(request.isVisible());
    }
}
