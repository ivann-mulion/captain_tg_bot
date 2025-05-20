package com.mulion.telegram_bot_application.services;

import com.mulion.models.User;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class MessageService extends DefaultAbsSender {
    public MessageService(DefaultBotOptions options, String botToken) {
        super(options, botToken);
    }

    public void sendText(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void editMessage(EditMessageReplyMarkup message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendInlineKeyboard(User user, List<List<InlineKeyboardButton>> rows, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(user.getChatId().toString());
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        user.setBotLastMessageId(sendMessage(message));
    }

    public void removeInlineButtons(User user) {
        if (user.getBotLastMessageId() == null) return;
        long chatId = user.getChatId();
        int messageId = user.getBotLastMessageId();

        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(messageId);
        editMarkup.setReplyMarkup(null);

        editMessage(editMarkup);
        user.setBotLastMessageId(null);
    }

    private Integer sendMessage(SendMessage message) {
        try {
            return execute(message).getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }
}
