package com.mulion.data_base.services;

import com.mulion.data_base.repositories.Repository;
import com.mulion.models.enums.Action;
import com.mulion.models.enums.UserRole;
import com.mulion.models.ActionSteps;
import com.mulion.models.User;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DBUserService {
    private final Repository<User, Long> repository;
    private final DBBoatService boatService;

    public User getUser(Long userId) {
        return repository.findById(userId).orElse(null);
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
                .actionStep(new ActionSteps())
                .role(UserRole.CAPTAIN)
                .build();
        repository.create(user);
        return user;
    }

    public void updateUser(User user) {
        repository.update(user);
    }

    public boolean addBoatToUser(Long userId, Long boatId) {
        try {
            User user = getUser(userId);
            user.addBoat(boatService.getBoat(boatId));
            updateUser(user);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    public boolean removeUsersBoat(Long userId, Long boatId) {
        try {
            User user = getUser(userId);
            user.removeBoat(boatService.getBoat(boatId));
            updateUser(user);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    public boolean setUserRole(Long userId, UserRole userRole) {
        try {
            User user = getUser(userId);
            user.setRole(userRole);
            updateUser(user);
            return true;
        } catch (RuntimeException _) {
            return false;
        }
    }

    public int nextStep(User user) {
        int step = user.getActionStep().nextStep();
        updateUser(user);
        return step;
    }

    public void restartAction(User user) {
        user.getActionStep().restartAction();
        updateUser(user);
    }

    public void inactive(User user) {
        user.getActionStep().inactivate();
        updateUser(user);
    }
}
