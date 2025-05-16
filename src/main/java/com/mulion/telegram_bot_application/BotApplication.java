package com.mulion.telegram_bot_application;

import com.mulion.data_base.SessionProvider;
import com.mulion.data_base.repository.UserRepository;
import com.mulion.data_base.services.DBUserService;
import com.mulion.enums.RegistrationStatus;
import com.mulion.enums.UserRole;
import com.mulion.models.User;
import com.mulion.telegram_bot_application.enums.Date;
import com.mulion.services.ConfigService;
import com.mulion.services.ReportService;
import com.mulion.constants.Config;
import com.mulion.telegram_bot_application.services.MessageService;
import com.mulion.yclients.services.YCUserService;
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

    private final DBUserService userService;
    private final AdminBotInterface adminInterface;
    private final MessageService messageService;

    public BotApplication(String token) {
        super(token);
        userService = new DBUserService(new UserRepository(new SessionProvider().getSessionFactory()));
        adminInterface = new AdminBotInterface();
        messageService = new MessageService(getOptions(), token);
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new BotApplication(ConfigService.getProperty(TOKEN)));
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
        if (!(update.hasMessage() && update.getMessage().hasText())) {
            return;
        }

        long userId = update.getMessage().getFrom().getId();
        User user = userService.getUser(userId);
        if (user == null) {
            user = userService.addUser(userId, update.getMessage().getFrom().getUserName(), update.getMessage().getFrom().getFirstName());
            registration(user, update);
            return;
        }
        if (user.getSteps().getRegistrationStatus() != RegistrationStatus.DONE) {
            registration(user, update);
            return;
        }
        if (user.getRole() == UserRole.ADMIN) {
            adminInterface.onUpdateReceived(user, update);
        }
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.matches("\\d{2}.\\d{2}.\\d{4}")) {
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

    private void registration(User user, Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        RegistrationStatus status = user.getSteps().getRegistrationStatus();
        switch (status) {
            case START -> {
                String helloMessage = String.format(
                        """
                                ола, капитан
                                чтобы продолжить работу нужно залогиниться
                                """
                );
                messageService.sendText(chatId, helloMessage);
                messageService.sendText(chatId, "введи свой логин от ЮК");
                user.getSteps().setRegistrationStatus(RegistrationStatus.LOGIN);
                userService.updateUser(user);
            }
            case LOGIN -> {
                messageService.sendText(chatId, "кул, теперь пароль");
                user.setLogin(messageText);
                user.getSteps().setRegistrationStatus(RegistrationStatus.PASSWORD);
                userService.updateUser(user);
            }
            case PASSWORD -> {
                user.setPassword(messageText);
                if (!YCUserService.authorization(user)) {
                    messageService.sendText(chatId, """
                            кажется, ты где-то ошибся
                            или может тебя уволили?
                            попробуем снова
                            """);
                    user.getSteps().setRegistrationStatus(RegistrationStatus.START);
                    registration(user, update);
                    return;
                }
                user.getSteps().setRegistrationStatus(RegistrationStatus.DONE);
                userService.updateUser(user);
                messageService.sendText(chatId, "велкам!");
                onUpdateReceived(update);
            }
        }
        System.out.println(user);
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
            case CUSTOM_DATE -> messageService.sendText(chatId, INPUT_DATE_MESSAGE);
            default -> sendInlineKeyboard(chatId);
        }
    }

    private void sendReport(long chatId, long userId, LocalDate date) {
        messageService.sendText(chatId, ReportService.getReportMessage(userService.getUser(userId), date));
    }
}
