package com.mulion.data_base.services;

import com.mulion.data_base.repository.Repository;
import com.mulion.enums.RegistrationStatus;
import com.mulion.models.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class DBUserService {
    private final Repository<User, Long> repository;

    public User getUser(Long userId) {
        return repository.findById(userId).orElse(null);
    }

    public User addUser(Long userId, String tgUserName, String name) {
        User user = User.builder()
                .id(userId)
                .tgUserName(tgUserName)
                .name(name)
                .registrationStatus(RegistrationStatus.START)
                .build();
        repository.create(user);
        return user;
    }

    public void updateUser(User user) {
        repository.update(user);
    }
}
