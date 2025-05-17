package com.mulion.telegram_bot_application.role_interfaces;

import com.mulion.constants.BotMassageTexts;
import com.mulion.constants.Config;
import com.mulion.data_base.services.DBUserService;
import com.mulion.models.User;
import com.mulion.models.enums.Action;
import com.mulion.models.enums.UserRole;
import com.mulion.services.ReportService;
import com.mulion.telegram_bot_application.enums.DateMenu;
import com.mulion.telegram_bot_application.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CaptainBotInterface {
    private final MessageService messageService;
    private final DBUserService userService;
    private final AdminBotInterface adminInterface;

    public void onUpdateReceived(User user, Update update) {
        if (user.getStep().getAction().getAccess() != UserRole.CAPTAIN) {
            adminInterface.onUpdateReceived(user, update);
        }

        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery(), user);
            return;
        }

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.matches("\\d{2}.\\d{2}.\\d{4}")) {
            try {
                LocalDate date = LocalDate.parse(messageText, Config.reportDateFormatter);
                sendReport(chatId, user.getId(), date);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        sendInlineKeyboard(user);
    }

    private void handleCallback(CallbackQuery callbackQuery, User user) {
        String data = callbackQuery.getData();
        DateMenu date = DateMenu.getDateFromString(data);
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        switch (date) {
            case YESTERDAY -> sendReport(chatId, userId, LocalDate.now().minusDays(1));
            case TODAY -> sendReport(chatId, userId, LocalDate.now());
            case CUSTOM_DATE -> messageService.sendText(chatId, BotMassageTexts.INPUT_DATE_MESSAGE);
            case MENU -> {
                adminInterface.sendAdminMenu(user);
                user.getStep().setAction(Action.MENU);
            }
        }
        sendInlineKeyboard(user);
    }

    private void sendReport(long chatId, long userId, LocalDate date) {
        messageService.sendText(chatId, ReportService.getReportMessage(userService.getUser(userId), date));
    }

    private void sendInlineKeyboard(User user) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(user.getChatId()));
        message.setText(BotMassageTexts.CHOOSE_DATE_MESSAGE);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                InlineKeyboardButton.builder().text("📆 вчера").callbackData(DateMenu.YESTERDAY.getAction()).build(),
                InlineKeyboardButton.builder().text("📅 сегодня").callbackData(DateMenu.TODAY.getAction()).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("🗓 за дату").callbackData(DateMenu.CUSTOM_DATE.getAction()).build()
        ));
        if (user.getRole() == UserRole.ADMIN) {
            rows.add(List.of(
                    InlineKeyboardButton.builder().text("меню").callbackData(DateMenu.MENU.getAction()).build()
            ));
        }

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        messageService.sendMessage(message);
    }
}
