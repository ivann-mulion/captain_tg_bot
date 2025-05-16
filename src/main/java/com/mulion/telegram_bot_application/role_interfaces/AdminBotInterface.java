package com.mulion.telegram_bot_application.role_interfaces;

import com.mulion.data_base.services.DBUserService;
import com.mulion.models.User;
import com.mulion.telegram_bot_application.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AdminBotInterface {
    public static final String ADD_BOAT_TO_CAPTAIN = "add_boat_to_captain";
    public static final String ADD_BOAT_IN_SYSTEM = "add_boat_in_system";
    private final MessageService messageService;
    private final DBUserService userService;

    public void onUpdateReceived(User user, Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }
        sendInlineKeyboard(update.getMessage().getChatId(), getMenuInlineButtons());
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        switch (data) {
            case ADD_BOAT_TO_CAPTAIN -> sendInlineKeyboard(chatId, getAddBoatToCaptainInlineButtons());
            case ADD_BOAT_IN_SYSTEM -> messageService.sendText(chatId, "добавить лодку в систему");
            default -> sendInlineKeyboard(chatId, getMenuInlineButtons());
        }
    }

    private void sendInlineKeyboard(long chatId, List<List<InlineKeyboardButton>> rows) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("меню");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        messageService.sendMessage(message);
    }

    private List<List<InlineKeyboardButton>> getAddBoatToCaptainInlineButtons() {
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

    private List<List<InlineKeyboardButton>> getMenuInlineButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                InlineKeyboardButton.builder().text("добавить яхту капитану").callbackData(ADD_BOAT_TO_CAPTAIN).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("добавить яхту").callbackData(ADD_BOAT_IN_SYSTEM).build()
        ));

        return rows;
    }
}
