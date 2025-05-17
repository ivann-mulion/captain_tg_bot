package com.mulion.data_base.services;

import com.mulion.data_base.repository.Repository;
import com.mulion.models.enums.UserRole;
import com.mulion.models.Step;
import com.mulion.models.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import java.util.List;

@RequiredArgsConstructor
public class DBUserService {
    private final Repository<User, Long> repository;

    public User getUser(Long userId) {
        return repository.findById(userId).orElse(null);
    }

    public User getUserWithBoats(Long userId) {
        try (Session session = repository.getSessionFactory().openSession()) {
            User user = session.get(User.class, userId);
            Hibernate.initialize(user.getBoats());
            return user;
        }
    }

    public List<User> getUsers() {
        return repository.findAll();
    }

    public User addUser(Long userId, Long chatId, String tgUserName, String name) {
        User user = User.builder()
                .id(userId)
                .tgUserName(tgUserName)
                .chatId(chatId)
                .name(name)
                .step(new Step())
                .role(UserRole.CAPTAIN)
                .build();
        repository.create(user);
        return user;
    }

    public void updateUser(User user) {
        repository.update(user);
    }
}
