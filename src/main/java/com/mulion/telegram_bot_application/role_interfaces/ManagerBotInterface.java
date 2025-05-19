package com.mulion.telegram_bot_application.role_interfaces;

import com.mulion.data_base.services.DBBoatService;
import com.mulion.data_base.services.DBUserService;
import com.mulion.models.User;
import com.mulion.models.enums.Action;
import com.mulion.models.enums.UserRole;
import com.mulion.telegram_bot_application.services.InterfaceService;
import com.mulion.telegram_bot_application.services.MessageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ManagerBotInterface {
    public static final String MANAGER_ACCESS_ERROR = "wtf man u r not a manager";

    private final MessageService messageService;
    private final InterfaceService interfaceService;
    private final DBUserService userService;
    @Getter
    private final CaptainBotInterface captainInterface;
    private Long bufferUserId;

    public ManagerBotInterface(
            MessageService messageService,
            InterfaceService interfaceService,
            DBBoatService boatService,
            DBUserService userService,
            AdminBotInterface adminInterface) {
        this.messageService = messageService;
        this.interfaceService = interfaceService;
        this.userService = userService;
        this.captainInterface = new CaptainBotInterface(
                messageService,
                interfaceService,
                boatService,
                userService,
                adminInterface,
                this
        );
    }

    public void onUpdateReceived(User user, Update update) {
        if (!checkUserAccess(user)) {
            return;
        }

        Action action = user.getActionStep().getAction();

        try {
            switch (action) {
                case ADD_BOAT_TO_CAPTAIN, REMOVE_CAPTAINS_BOAT -> captainsBoatActions(user, update);
                default -> managerMenu(user, update);
            }
        } catch (Exception _) {
            sendMenu(user);
        }
    }

    public void managerMenu(User user, Update update) {
        Action action = Action.valueOf(update.getCallbackQuery().getData());

        switch (action) {
            case ADD_BOAT_TO_CAPTAIN -> {
                userService.setAction(user, Action.ADD_BOAT_TO_CAPTAIN);
                captainsBoatActions(user, update);
            }
            case REMOVE_CAPTAINS_BOAT -> {
                userService.setAction(user, Action.REMOVE_CAPTAINS_BOAT);
                captainsBoatActions(user, update);
            }
            default -> captainInterface.sendBaseMenu(user);
        }
    }

    private boolean checkUserAccess(User user) {
        if (user.getRole() == UserRole.CAPTAIN) {
            messageService.sendText(user.getChatId(), MANAGER_ACCESS_ERROR);
            captainInterface.sendBaseMenu(user);
            return false;
        }
        return true;
    }

    private void captainsBoatActions(User user, Update update) {
        int step = userService.nextStep(user);

        if (step == 0) {
            interfaceService.sendCaptains(user);
            return;
        }

        Long id = interfaceService.getId(user, update);
        if (id == null) {
            sendMenu(user);
            return;
        }

        switch (step) {
            case 1 -> {
                bufferUserId = id;
                if (user.getActionStep().getAction() == Action.ADD_BOAT_TO_CAPTAIN) {
                    interfaceService.sendAllBoats(user);
                } else {
                    interfaceService.sendCaptainsBoats(user, userService.getUserWithBoats(id));
                }
            }
            case 2 -> {
                boolean isOk;
                if (user.getActionStep().getAction() == Action.ADD_BOAT_TO_CAPTAIN) {
                    isOk = userService.addBoatToUser(bufferUserId, id);
                } else {
                    isOk = userService.removeUsersBoat(bufferUserId, id);
                }
                user = interfaceService.updateAdminUser(user, bufferUserId);
                if (isOk) {
                    messageService.sendText(user.getChatId(), "ok");
                } else {
                    messageService.sendText(user.getChatId(), "something wrong");
                }
                sendMenu(user);
            }
            default -> messageService.sendText(user.getChatId(), "logger error");
        }
    }

    public void sendMenu(User user) {
        userService.setAction(user, Action.MANAGER_MENU);
        messageService.sendInlineKeyboard(user, getMenuInlineButtons(), "menu");
    }

    private List<List<InlineKeyboardButton>> getMenuInlineButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(
                InlineKeyboardButton.builder().text("add cap boat").callbackData(Action.ADD_BOAT_TO_CAPTAIN.toString()).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("remove caps boat").callbackData(Action.REMOVE_CAPTAINS_BOAT.toString()).build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("back").callbackData(Action.INACTIVE.toString()).build()
        ));

        return rows;
    }
}
