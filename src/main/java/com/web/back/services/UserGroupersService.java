package com.web.back.services;

import com.web.back.model.dto.UserGroupersDto;
import com.web.back.model.entities.UserGrouper;
import com.web.back.model.requests.UserGropersRequest;
import com.web.back.repositories.UserGroupersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserGroupersService {
    private final UserGroupersRepository userGroupersRepository;

    public UserGroupersService(UserGroupersRepository userGroupersRepository) {
        this.userGroupersRepository = userGroupersRepository;
    }

    public List<UserGroupersDto> getAll() {
        return userGroupersRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UserGroupersDto getById(Long id) {
        return userGroupersRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    public List<UserGroupersDto> getAllByUserId(int userId) {
        return userGroupersRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<UserGroupersDto> add(UserGropersRequest request) {
        UserGrouper entity = mapToEntity(request);
        UserGrouper saved = userGroupersRepository.save(entity);
        return List.of(mapToDto(saved));
    }

    public List<UserGroupersDto> update(Long id, UserGropersRequest request) {
        Optional<UserGrouper> existing = userGroupersRepository.findById(id);
        if (existing.isPresent()) {
            UserGrouper entity = existing.get();
            entity.setGrouper1(request.grouper1());
            entity.setGrouper2(request.grouper2());
            entity.setGrouper3(request.grouper3());
            entity.setGrouper4(request.grouper4());
            entity.setGrouper5(request.grouper5());

            UserGrouper updated = userGroupersRepository.save(entity);
            return List.of(mapToDto(updated));
        }
        return List.of();
    }

    public void deleteById(Long id) {
        userGroupersRepository.deleteById(id);
    }

    private UserGroupersDto mapToDto(UserGrouper entity) {
        return new UserGroupersDto(
                entity.getId(),
                entity.getUser().getId(),
                entity.getUser().getUsername(),
                entity.getUser().getName(),
                entity.getGrouper1(),
                entity.getGrouper2(),
                entity.getGrouper3(),
                entity.getGrouper4(),
                entity.getGrouper5()
        );
    }

    private UserGrouper mapToEntity(UserGropersRequest request) {
        var entity = new UserGrouper();
        var user = new com.web.back.model.entities.User();
        user.setId(request.userId());
        entity.setUser(user);
        entity.setGrouper1(request.grouper1());
        entity.setGrouper2(request.grouper2());
        entity.setGrouper3(request.grouper3());
        entity.setGrouper4(request.grouper4());
        entity.setGrouper5(request.grouper5());
        return entity;
    }
}
