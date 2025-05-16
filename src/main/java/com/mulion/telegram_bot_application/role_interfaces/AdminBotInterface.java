package com.mulion.telegram_bot_application.role_interfaces;

import com.mulion.data_base.services.DBUserService;
import com.mulion.models.User;
import com.mulion.telegram_bot_application.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class AdminBotInterface {
    private final MessageService messageService;
    private final DBUserService userService;

    public void onUpdateReceived(User user, Update update) {

    }
}
