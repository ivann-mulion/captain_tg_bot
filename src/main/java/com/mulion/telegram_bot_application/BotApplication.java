package com.mulion.telegram_bot_application;

import com.mulion.constants.BotMassageTexts;
import com.mulion.data_base.SessionProvider;
import com.mulion.data_base.repository.UserRepository;
import com.mulion.data_base.services.DBUserService;
import com.mulion.models.enums.UserRegistrationStatus;
import com.mulion.models.enums.UserRole;
import com.mulion.models.User;
import com.mulion.services.ConfigService;
import com.mulion.telegram_bot_application.role_interfaces.AdminBotInterface;
import com.mulion.telegram_bot_application.role_interfaces.CaptainBotInterface;
import com.mulion.telegram_bot_application.services.MessageService;
import com.mulion.yclients.services.YCUserService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class BotApplication extends TelegramLongPollingBot {
    public static final String TOKEN = "tg.token";
    public static final String BOT_USER_NAME = "tg.bot_user_name";

    private final DBUserService userService;
    private final MessageService messageService;
    private final AdminBotInterface adminInterface;
    private final CaptainBotInterface captainInterface;

    public BotApplication(String token) {
        super(token);
        userService = new DBUserService(new UserRepository(new SessionProvider().getSessionFactory()));
        messageService = new MessageService(getOptions(), token);
        adminInterface = new AdminBotInterface(messageService, userService);
        captainInterface = new CaptainBotInterface(messageService, userService);
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new BotApplication(ConfigService.getProperty(TOKEN)));
            System.out.println(BotMassageTexts.START_MESSAGE);
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
        if (!(update.hasMessage() && update.getMessage().hasText() || update.hasCallbackQuery())) {
            return;
        }

        long userId = update.getMessage().getFrom().getId();
        User user = userService.getUser(userId);

        if (!checkUser(update, user, userId)) return;

        UserRole role = user.getRole();
        switch (role) {
            case ADMIN -> adminInterface.onUpdateReceived(user, update);
            default -> captainInterface.onUpdateReceived(user, update);
        }
    }

    private boolean checkUser(Update update, User user, long userId) {
        if (user == null) {
            user = userService.addUser(userId, update.getMessage().getFrom().getUserName(), update.getMessage().getFrom().getFirstName());
            messageService.sendText(update.getMessage().getChatId(), BotMassageTexts.REGISTRATION_GREETENG);
            registration(user, update);
            return false;
        }
        if (user.getSteps().getRegistrationStatus() != UserRegistrationStatus.DONE) {
            registration(user, update);
            return false;
        }
        return true;
    }

    private void registration(User user, Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        UserRegistrationStatus status = user.getSteps().getRegistrationStatus();
        switch (status) {
            case START -> {
                messageService.sendText(chatId, BotMassageTexts.REGISTRATION_LOGIN);
                user.getSteps().setRegistrationStatus(UserRegistrationStatus.LOGIN);
                userService.updateUser(user);
            }
            case LOGIN -> {
                messageService.sendText(chatId, BotMassageTexts.REGISTRATION_PASSWORD);
                user.setLogin(messageText);
                user.getSteps().setRegistrationStatus(UserRegistrationStatus.PASSWORD);
                userService.updateUser(user);
            }
            case PASSWORD -> {
                user.setPassword(messageText);
                if (!YCUserService.authorization(user)) {
                    messageService.sendText(chatId, BotMassageTexts.REGISTRATION_ERROR);
                    user.getSteps().setRegistrationStatus(UserRegistrationStatus.START);
                    registration(user, update);
                    return;
                }
                user.getSteps().setRegistrationStatus(UserRegistrationStatus.DONE);
                userService.updateUser(user);
                messageService.sendText(chatId, BotMassageTexts.REGISTRATION_DONE);
                onUpdateReceived(update);
            }
        }
        System.out.println(user);
    }
}
