package com.mulion.telegram_bot_application;

import com.mulion.enums.Date;
import com.mulion.services.ConfigService;
import com.mulion.services.ReportService;
import com.mulion.services.UserService;
import com.mulion.constants.Config;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BotApplication extends TelegramLongPollingBot {
    public static final String INPUT_DATE_MESSAGE = "Введите дату в формате дд.мм.гггг (например, 01.01.2025) :";
    public static final String START_MESSAGE = "Бот запущен";
    public static final String TOKEN = "tg.token";
    public static final String BOT_USER_NAME = "tg.bot_user_name";
    public static final String CHOOSE_DATE_MESSAGE = "Выберите дату отчета:";

    public BotApplication() {
        super(ConfigService.getProperty(TOKEN));
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new BotApplication());
            System.out.println(START_MESSAGE);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return ConfigService.getProperty(BOT_USER_NAME);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }
        if (!(update.hasMessage() && update.getMessage().hasText())) {
            return;
        }

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.matches("\\d{2}.\\d{2}.\\d{4}")) {
            long userId = update.getMessage().getFrom().getId();
            try {
                LocalDate date = LocalDate.parse(messageText, Config.reportDateFormatter);
                sendReport(chatId, userId, date);
                return;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        sendInlineKeyboard(chatId);
    }

    private void sendInlineKeyboard(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(CHOOSE_DATE_MESSAGE);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                InlineKeyboardButton.builder().text("📆 вчера").callbackData("yesterday").build(),
                InlineKeyboardButton.builder().text("📅 сегодня").callbackData("today").build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("🗓 за дату").callbackData("custom_date").build()
        ));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Date date = Date.getDateFromString(data);
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        switch (date) {
            case YESTERDAY -> sendReport(chatId, userId, LocalDate.now().minusDays(1));
            case TODAY -> sendReport(chatId, userId, LocalDate.now());
            case CUSTOM_DATE -> sendText(chatId, INPUT_DATE_MESSAGE);
            default -> sendInlineKeyboard(chatId);
        }
    }

    private void sendReport(long chatId, long userId, LocalDate date) {
        sendText(chatId, ReportService.getReportMessage(UserService.getUser(userId), date));
    }

    private void sendText(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
