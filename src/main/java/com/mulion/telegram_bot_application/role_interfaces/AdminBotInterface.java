package com.mulion.telegram_bot_application.role_interfaces;

import com.mulion.data_base.services.DBBoatService;
import com.mulion.data_base.services.DBUserService;
import com.mulion.models.Boat;
import com.mulion.models.User;
import com.mulion.models.enums.Action;
import com.mulion.models.enums.UserRole;
import com.mulion.telegram_bot_application.services.MessageService;
import com.mulion.yclients.services.YCBoatService;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class AdminBotInterface {
    private final MessageService messageService;
    private final DBUserService userService;
    private final DBBoatService boatService;
    @Getter
    private final CaptainBotInterface captainInterface;
    private Long bufferUserId;
    private Boat.BoatBuilder boatBuilder;

    public AdminBotInterface(MessageService messageService, DBUserService userService, DBBoatService boatService) {
        this.messageService = messageService;
        this.userService = userService;
        this.boatService = boatService;
        this.captainInterface = new CaptainBotInterface(messageService, userService, this);
    }

    public void onUpdateReceived(User user, Update update) {
        if (!checkUserAccess(user)) {
            return;
        }

        Action action = user.getActionStep().getAction();

        try {
            switch (action) {
                case ADD_BOAT_IN_SYSTEM -> addBoatInSystem(user, update);
                case ADD_BOAT_TO_CAPTAIN, REMOVE_CAPTAINS_BOAT -> captainsBoatActions(user, update);
                default -> adminMenu(user, update);
            }
        } catch (Exception e) {
            sendMenu(user);
        }
    }

    private boolean checkUserAccess(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            System.out.println("ALARM");
            messageService.sendText(user.getChatId(), "wrf man u r not an admin");
            userService.inactive(user);
            return false;
        }
        return true;
    }

    private void addBoatInSystem(User user, Update update) {
        int step = userService.nextStep(user);

        if (step == 0) {
            messageService.sendText(user.getChatId(), "введи staff_id яхты");
            return;
        }

        String message = update.getMessage().getText();

        switch (step) {
            case 1 -> {
                try {
                    Long boatId = Long.valueOf(message);
                    if (!YCBoatService.isBoat(boatId)) {
                        throw new NumberFormatException();
                    }
                    boatBuilder = Boat.builder().id(boatId);
                    messageService.sendText(user.getChatId(), "теперь давай имя яхты");
                } catch (NumberFormatException e) {
                    messageService.sendText(user.getChatId(), "некорректный staff_id, все по новой давай");
                    sendMenu(user);
                }
            }
            case 2 -> {
                Boat boat = boatBuilder.name(message).build();
                boatBuilder = null;
                boat = boatService.addBoat(boat);
                messageService.sendText(user.getChatId(), "успешно добавлена яхта" + boat);
                sendMenu(user);
            }
            default -> messageService.sendText(user.getChatId(), "logger error");
        }
    }

    private void captainsBoatActions(User user, Update update) {
        int step = userService.nextStep(user);

        if (step == 0) {
            sendCaptains(user);
            return;
        }

        long chatId = user.getChatId();

        Long id = null;
        try {
            id = Long.valueOf(update.getCallbackQuery().getData());
        } catch (NumberFormatException e) {
            messageService.sendText(chatId, "id parsing error - " + id);
        }

        switch (step) {
            case 1 -> {
                bufferUserId = id;
                if (user.getActionStep().getAction() == Action.ADD_BOAT_TO_CAPTAIN) {
                    sendAllBoats(user);
                } else {
                    sendUsersBoats(user, userService.getUserWithBoats(id));
                }
            }
            case 2 -> {
                boolean isOk;
                if (user.getActionStep().getAction() == Action.ADD_BOAT_TO_CAPTAIN) {
                    isOk = userService.addBoatToUser(bufferUserId, id);
                } else {
                    isOk = userService.removeUsersBoat(bufferUserId, id);
                }
                user = updateAdminUser(user);
                if (isOk) {
                    messageService.sendText(chatId, "ok");
                } else {
                    messageService.sendText(chatId, "something wrong");
                }
                sendMenu(user);
            }
            default -> messageService.sendText(user.getChatId(), "logger error");
        }
    }

    private User updateAdminUser(User user) {
        if (bufferUserId.equals(user.getId())) {
            user = userService.getUser(bufferUserId);
        }
        return user;
    }

    public void sendMenu(User user) {
        sendInlineKeyboard(user, getMenuInlineButtons(), "menu");
        userService.setAction(user, Action.MENU);
    }

    private void sendCaptains(User user) {
        sendInlineKeyboard(user, getUsersInlineButtons(), "choose captain");
    }

    private void sendAllBoats(User user) {
        sendInlineKeyboard(user, getBoatsInlineButtons(boatService.getBoats()), "choose boat");
    }

    private void sendUsersBoats(User userChat, User userBoats) {
        sendInlineKeyboard(userChat, getBoatsInlineButtons(userBoats.getBoats().stream().toList()), "choose boat");
    }

    public void adminMenu(User user, Update update) {
        Action action = Action.valueOf(update.getCallbackQuery().getData());

        switch (action) {
            case ADD_BOAT_IN_SYSTEM -> {
                userService.setAction(user, Action.ADD_BOAT_IN_SYSTEM);
                addBoatInSystem(user, update);
            }
            case ADD_BOAT_TO_CAPTAIN -> {
                userService.setAction(user, Action.ADD_BOAT_TO_CAPTAIN);
                captainsBoatActions(user, update);
            }
            case REMOVE_CAPTAINS_BOAT -> {
                userService.setAction(user, Action.REMOVE_CAPTAINS_BOAT);
                captainsBoatActions(user, update);
            }
            default -> captainInterface.sendMenu(user);
        }
    }

    private void sendInlineKeyboard(User user, List<List<InlineKeyboardButton>> rows, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(user.getChatId()));
        message.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        messageService.sendMessage(message);
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

    private List<List<InlineKeyboardButton>> getBoatsInlineButtons(List<Boat> boats) {
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

    private List<List<InlineKeyboardButton>> getMenuInlineButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                InlineKeyboardButton.builder().text("добавить яхту капитану").callbackData(Action.ADD_BOAT_TO_CAPTAIN.toString()).build(),
                InlineKeyboardButton.builder().text("удалить яхту капитана").callbackData(Action.REMOVE_CAPTAINS_BOAT.toString()).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("добавить яхту").callbackData(Action.ADD_BOAT_IN_SYSTEM.toString()).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("назад").callbackData(Action.INACTIVE.toString()).build()
        ));

        return rows;
    }
}
