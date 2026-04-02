package com.malllite.user.service;

import com.malllite.user.dto.UserResponse;
import com.malllite.user.model.User;
import com.malllite.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> listUsers() {
        List<User> users = userRepository.findAll();
        Map<Long, List<String>> roleCodesByUserId = userRepository.findRoleCodesByUserIds(
                users.stream().map(User::id).toList()
        );

        return users
                .stream()
                .map(user -> toResponse(user, roleCodesByUserId.getOrDefault(user.id(), List.of())))
                .toList();
    }

    private UserResponse toResponse(User user, List<String> roleCodes) {
        return new UserResponse(
                user.id(),
                user.username(),
                user.nickname(),
                user.email(),
                user.phone(),
                user.enabled(),
                roleCodes
        );
    }
}
