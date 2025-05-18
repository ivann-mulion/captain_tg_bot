package com.mulion.telegram_bot_application.role_interfaces;

import com.mulion.constants.BotMassageTexts;
import com.mulion.constants.Config;
import com.mulion.data_base.services.DBUserService;
import com.mulion.models.Boat;
import com.mulion.models.User;
import com.mulion.models.enums.Action;
import com.mulion.models.enums.UserRole;
import com.mulion.services.ReportService;
import com.mulion.telegram_bot_application.enums.DateMenu;
import com.mulion.telegram_bot_application.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
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
        Action action = user.getActionStep().getAction();

        try {
            switch (action) {
                case CAPTAIN_MENU -> captainMenu(user, update);
                case CHANGE_BOAT -> changeBot(user, update);
                default -> defaultReceived(user, update);
            }
        } catch (RuntimeException e) {
            sendBaseMenu(user);
        }
    }

    private void defaultReceived(User user, Update update) {
        if (update.hasCallbackQuery()) {
            baseMenu(user, update.getCallbackQuery());
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

        sendBaseMenu(user);
    }

    private void baseMenu(User user, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        DateMenu date = DateMenu.valueOf(data);
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        switch (date) {
            case YESTERDAY -> sendReport(chatId, userId, LocalDate.now().minusDays(1));
            case TODAY -> sendReport(chatId, userId, LocalDate.now());
            case CUSTOM_DATE -> messageService.sendText(chatId, BotMassageTexts.INPUT_DATE_MESSAGE);
            case CAPTAIN_MENU -> {
                sendCaptainMenu(user);
                return;
            }
            case ADMIN_MENU -> {
                adminInterface.sendMenu(user);
                return;
            }
        }
        sendBaseMenu(user);
    }

    private void captainMenu(User user, Update update) {
        Action action = Action.valueOf(update.getCallbackQuery().getData());

        switch (action) {
            case CHANGE_BOAT -> {
                userService.setAction(user, Action.CHANGE_BOAT);
                changeBot(user, update);
            }
            default -> sendBaseMenu(user);
        }
    }

    private void changeBot(User user, Update update) {
        int step = userService.nextStep(user);

        if (step == 0) {
            sendCaptainsBoats(userService.getUserWithBoats(user.getId()));
            return;
        }

        Long id = getId(user, update);
        if (id == null) {
            sendBaseMenu(user);
            return;
        }

        user.setStaffId(id);
        sendBaseMenu(user);
    }

    private void sendReport(long chatId, long userId, LocalDate date) {
        messageService.sendText(chatId, ReportService.getReportMessage(userService.getUser(userId), date));
    }

    public void sendBaseMenu(User user) {
        userService.inactive(user);
        messageService.sendInlineKeyboard(user, getBaseMenuInlineButtons(user), BotMassageTexts.CHOOSE_DATE_MESSAGE);
    }

    private void sendCaptainMenu(User user) {
        userService.setAction(user, Action.CAPTAIN_MENU);
        messageService.sendInlineKeyboard(user, getCaptainInlineButtons(), "menu");
    }

    private void sendCaptainsBoats(User user) {
        messageService.sendInlineKeyboard(user, getBoatsInlineButtons(user.getBoats().stream().toList()), "choose boat");
    }

    private List<List<InlineKeyboardButton>> getBaseMenuInlineButtons(User user) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                InlineKeyboardButton.builder().text("📆 вчера").callbackData(DateMenu.YESTERDAY.toString()).build(),
                InlineKeyboardButton.builder().text("📅 сегодня").callbackData(DateMenu.TODAY.toString()).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("🗓 за дату").callbackData(DateMenu.CUSTOM_DATE.toString()).build()
        ));
        if (user.getBoatsCount() != null && user.getBoatsCount() > 1) {

            rows.add(List.of(
                    InlineKeyboardButton.builder().text("меню").callbackData(DateMenu.CAPTAIN_MENU.toString()).build()
            ));
        }
        if (user.getRole() == UserRole.MANAGER || user.getRole() == UserRole.ADMIN) {
            rows.add(List.of(
                    InlineKeyboardButton.builder().text("manager").callbackData(DateMenu.MANAGER_MENU.toString()).build()
            ));
        }
        if (user.getRole() == UserRole.ADMIN) {
            rows.add(List.of(
                    InlineKeyboardButton.builder().text("admin").callbackData(DateMenu.ADMIN_MENU.toString()).build()
            ));
        }

        return rows;
    }

    private List<List<InlineKeyboardButton>> getCaptainInlineButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                InlineKeyboardButton.builder().text("change boat").callbackData(Action.CHANGE_BOAT.toString()).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("back").callbackData(Action.INACTIVE.toString()).build()
        ));

        return rows;
    }

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
        } catch (NumberFormatException e) {
            messageService.sendText(user.getChatId(), "id parsing error - " + id);
        }
        return id;
    }
}
