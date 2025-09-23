package com.web.back.services;

import com.web.back.model.dto.FestiveDayDto;
import com.web.back.model.entities.FestiveDay;
import com.web.back.model.requests.FestiveDayRequest;
import com.web.back.repositories.FestiveDaysRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FestiveDaysService {
    private final FestiveDaysRepository festiveDaysRepository;

    public FestiveDaysService(FestiveDaysRepository festiveDaysRepository) {
        this.festiveDaysRepository = festiveDaysRepository;
    }

    @Transactional(readOnly = true)
    public List<FestiveDayDto> getAll() {
        return festiveDaysRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FestiveDayDto getById(Long id) {
        FestiveDay festiveDay = festiveDaysRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festive day not found"));
        return toDto(festiveDay);
    }

    @Transactional
    public FestiveDayDto add(FestiveDayRequest request) {
        FestiveDay entity = fromRequest(request);
        FestiveDay saved = festiveDaysRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public FestiveDayDto update(Long id, FestiveDayRequest request) {
        FestiveDay entity = festiveDaysRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festive day not found"));
        updateEntity(entity, request);
        FestiveDay saved = festiveDaysRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        festiveDaysRepository.deleteById(id);
    }

    private FestiveDayDto toDto(FestiveDay entity) {
        return new FestiveDayDto(
                entity.getId(),
                entity.getDay(),
                entity.getMonth(),
                entity.getName(),
                entity.getDescription()
        );
    }

    private FestiveDay fromRequest(FestiveDayRequest request) {
        FestiveDay entity = new FestiveDay();
        entity.setDay(request.day());
        entity.setMonth(request.month());
        entity.setName(request.name());
        entity.setDescription(request.description());
        return entity;
    }

    private void updateEntity(FestiveDay entity, FestiveDayRequest request) {
        entity.setDay(request.day());
        entity.setMonth(request.month());
        entity.setName(request.name());
        entity.setDescription(request.description());
    }
}
