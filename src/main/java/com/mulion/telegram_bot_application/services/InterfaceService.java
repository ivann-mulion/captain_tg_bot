package com.mulion.telegram_bot_application.services;

import com.mulion.data_base.services.DBBoatService;
import com.mulion.data_base.services.DBUserService;
import com.mulion.models.Boat;
import com.mulion.models.User;
import com.mulion.models.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class InterfaceService {
    private final MessageService messageService;
    private final DBUserService userService;
    private final DBBoatService boatService;

    public List<List<InlineKeyboardButton>> getBoatsInlineButtons(List<Boat> boats) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Boat boat : boats) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(boat.getName())
                            .callbackData(boat.getId().toString())
                            .build()
            ));
        }

        return rows;
    }

    public Long getId(User user, Update update) {
        Long id = null;
        try {
            id = Long.valueOf(update.getCallbackQuery().getData());
        } catch (NumberFormatException _) {
            messageService.sendText(user.getChatId(), "id parsing error - " + update.getCallbackQuery().getData());
        }
        return id;
    }

    public void sendCaptains(User user) {
        messageService.sendInlineKeyboard(user, getUsersInlineButtons(), "choose captain");
    }

    public void sendAllBoats(User user) {
        messageService.sendInlineKeyboard(user,
                getBoatsInlineButtons(boatService.getBoats()),
                "choose boat");
    }

    public void sendCaptainsBoats(User userChat, User userBoats) {
        messageService.sendInlineKeyboard(userChat,
                getBoatsInlineButtons(userBoats.getBoats().stream().toList()),
                "choose boat");
    }

    public void sendRoles(User user) {
        messageService.sendInlineKeyboard(user, getRolesInlineButtons(), "choose role");
    }

    private List<List<InlineKeyboardButton>> getUsersInlineButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<User> users = userService.getUsers();

        for (User user : users) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(user.getName())
                            .callbackData(user.getId().toString())
                            .build()
            ));
        }

        return rows;
    }

    private List<List<InlineKeyboardButton>> getRolesInlineButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (UserRole role : UserRole.values()) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(role.toString())
                            .callbackData(role.toString())
                            .build()
            ));
        }

        return rows;
    }

    public void updateAdminUser(User user, Long userId) {
        if (userId.equals(user.getId())) {
            User newUser = userService.getUser(userId);
            user.setRole(newUser.getRole());
            user.setStaffId(newUser.getStaffId());
            user.setBoatsCount(newUser.getBoatsCount());
        }
    }
}
