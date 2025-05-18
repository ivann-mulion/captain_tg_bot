package com.mulion.telegram_bot_application.role_interfaces;

import com.mulion.data_base.services.DBBoatService;
import com.mulion.data_base.services.DBUserService;
import com.mulion.models.Boat;
import com.mulion.models.User;
import com.mulion.models.enums.Action;
import com.mulion.models.enums.UserRole;
import com.mulion.telegram_bot_application.services.InterfaceService;
import com.mulion.telegram_bot_application.services.MessageService;
import com.mulion.yclients.services.YCBoatService;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class AdminBotInterface {
    public static final String ADMIN_ACCESS_ERROR = "wtf man u r not an admin";
    private final MessageService messageService;
    private final InterfaceService interfaceService;
    private final DBUserService userService;
    private final DBBoatService boatService;
    @Getter
    private final CaptainBotInterface captainInterface;
    @Getter
    private final ManagerBotInterface managerInterface;
    private Long bufferUserId;
    private Boat.BoatBuilder boatBuilder;

    public AdminBotInterface(MessageService messageService, DBUserService userService, DBBoatService boatService) {
        this.messageService = messageService;
        this.userService = userService;
        this.boatService = boatService;
        this.interfaceService = new InterfaceService(messageService, userService, boatService);
        this.managerInterface = new ManagerBotInterface(
                messageService,
                interfaceService,
                boatService,
                userService,
                this);
        this.captainInterface = managerInterface.getCaptainInterface();
    }

    public void onUpdateReceived(User user, Update update) {
        if (!checkUserAccess(user)) {
            return;
        }

        Action action = user.getActionStep().getAction();

        try {
            switch (action) {
                case ADD_BOAT_IN_SYSTEM -> addBoatInSystem(user, update);
                case SET_USERS_ROLE -> setUsersRole(user, update);
                default -> adminMenu(user, update);
            }
        } catch (Exception e) {
            sendMenu(user);
        }
    }

    public void adminMenu(User user, Update update) {
        Action action = Action.valueOf(update.getCallbackQuery().getData());

        switch (action) {
            case ADD_BOAT_IN_SYSTEM -> {
                userService.setAction(user, Action.ADD_BOAT_IN_SYSTEM);
                addBoatInSystem(user, update);
            }
            case SET_USERS_ROLE -> {
                userService.setAction(user, Action.SET_USERS_ROLE);
                setUsersRole(user, update);
            }
            default -> captainInterface.sendBaseMenu(user);
        }
    }

    private boolean checkUserAccess(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            messageService.sendText(user.getChatId(), ADMIN_ACCESS_ERROR);
            captainInterface.sendBaseMenu(user);
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

    private void setUsersRole(User user, Update update) {
        int step = userService.nextStep(user);

        if (step == 0) {
            interfaceService.sendCaptains(user);
            return;
        }

        switch (step) {
            case 1 -> {
                bufferUserId = interfaceService.getId(user, update);
                if (bufferUserId == null) {
                    sendMenu(user);
                    return;
                }
                interfaceService.sendRoles(user);
            }
            case 2 -> {
                UserRole role = UserRole.valueOf(update.getCallbackQuery().getData());
                if (userService.setUserRole(bufferUserId, role)) {
                    messageService.sendText(user.getChatId(), "ok");
                } else {
                    messageService.sendText(user.getChatId(), "something wrong");
                }
                user = interfaceService.updateAdminUser(user, bufferUserId);
                sendMenu(user);
            }
            default -> messageService.sendText(user.getChatId(), "logger error");
        }
    }

    public void sendMenu(User user) {
        userService.setAction(user, Action.ADMIN_MENU);
        messageService.sendInlineKeyboard(user, getMenuInlineButtons(), "menu");
    }

    private List<List<InlineKeyboardButton>> getMenuInlineButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                InlineKeyboardButton.builder().text("set users role").callbackData(Action.SET_USERS_ROLE.toString()).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("add boat").callbackData(Action.ADD_BOAT_IN_SYSTEM.toString()).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("back").callbackData(Action.INACTIVE.toString()).build()
        ));

        return rows;
    }
}
