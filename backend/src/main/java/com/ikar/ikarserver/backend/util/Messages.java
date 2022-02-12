package com.ikar.ikarserver.backend.util;

public final class Messages {

    public static final String NOT_FOUND_USER_ERROR = "Пользователь не найден.";
    public static final String BUSY_USERNAME_ERROR = "Имя пользователя {0} уже используется.";
    public static final String EMPTY_USER_USERNAME_ERROR = "Логин пользователя не может быть пустым.";
    public static final String EMPTY_USER_PASSWORD_ERROR = "Пароль не может быть пустым";
    public static final String EMPTY_USER_FIRSTNAME_ERROR = "Имя пользователя не может быть пустым.";
    public static final String EMPTY_USER_SECONDNAME_ERROR = "Фамилия пользователя не может быть пустой.";
    public static final String UPDATES_USER_IS_NOT_AUTHORIZED = "Обновляемый пользователь не авторизирован.";
    public static final String PASSWORD_DOES_NOT_MATCH_ERROR = "Старый пароль не верен.";

    public static final String USER_IS_BROADCASTING = "Пользователь {0} ведет трансляцию";
    public static final String PRESENTER_BUSY = "В настоящее время презентующим выступает другой пользователь.";
    public static final String USER_ALREADY_PRESENTER = "Вы уже выступаете презентующим.";
    public static final String NOT_ACTIVE_PRESENTER = "Сейчас нет активного презентующего.";
    public static final String USER_ARE_NOT_PRESENTER = "Вы не являетесь перезентующим.";
    public static final String USER_START_PRESENTATION = "Пользователь {0} начал трансляцию";
    public static final String USER_STOP_PRESENTATION = "Пользователь {0} прекратил трансляцию";
    public static final String CALL_USER_NOT_EXIST = "Пользователь не найден.";

    public static final String ROOM_NOT_FOUND = "Комната не найдена.";
    public static final String ROOM_USER_NOT_FOUND = "Вы не являетесь участником комнаты.";
    public static final String ROOM_NEW_PARTICIPANT_ARRIVED = "Пользователь {0} поключился к комнате";
    public static final String ROOM_PARTICIPANT_LEFT = "Пользователь {0} покинул комнату";

    public static final String CONFERENCE_NOT_FOUND = "Конференция не найдена.";
    public static final String CONFERENCE_USER_EXIST = "Пользователь уже зарегистрирован в конференции.";
    public static final String CONFERENCE_USER_NOT_FOUND = "Пользователь не зарегистрирован в конференции.";
    public static final String CONFERENCE_NEW_USER_JOIN = "Пользователь {0} присоединился к конференции.";
    public static final String CONFERENCE_USER_LEAVE = "Пользователь {0} покинул конференцию.";
    public static final String CONFERENCE_DOES_NOT_PRESENTER = "Вы не являетесь презентующим";
    public static final String CONFERENCE_VIEWED_EXCEPTION = "Вы уже просматриваете эту конференцию.";

}
