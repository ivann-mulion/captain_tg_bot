package com.mulion.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BotMassageTexts {
    public static final String REGISTRATION_GREETENG = """
            ола, капитан
            чтобы продолжить работу нужно залогиниться
            """;
    public static final String REGISTRATION_PASSWORD = "кул, теперь пароль";
    public static final String REGISTRATION_ERROR = """
            кажется, ты где-то ошибся
            или может тебя уволили?
            попробуем снова
            """;
    public static final String REGISTRATION_DONE = "велкам!";
    public static final String REGISTRATION_LOGIN = "введи свой логин от ЮК";

    public static final String INPUT_DATE_MESSAGE = "Введите дату в формате дд.мм.гггг (например, 01.01.2025) :";
    public static final String START_MESSAGE = "Бот запущен";
    public static final String CHOOSE_DATE_MESSAGE = "Выберите дату отчета:";
}
