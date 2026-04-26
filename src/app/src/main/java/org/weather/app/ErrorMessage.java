package org.weather.app;

public final class ErrorMessage {
    private ErrorMessage() {}

    public static final String USER_NOT_FOUND = "User with this login was not found";
    public static final String CITY_NOT_FOUND = "City not found";

    public static final String USER_EXISTS = "User with this login already exists";

    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String INVALID_PARAMS = "Invalid user or password";
    public static final String INVALID_CITY = "Invalid city";

    public static final String TIMEOUT = "Timeout";

    public static final String INVALID_LOGIN_LENGTH = "Login length must be between 4 and 30 symbols";
    public static final String INVALID_LOGIN_SPECIAL_SYMBOLS = "Login can only contain numbers, letters, dashes and underscores";
    public static final String INVALID_LOGIN_FIRST_SYMBOL = "Login must begin with a letter";

    public static final String INVALID_PASSWORD_LENGTH = "Password length must be between 8 and 40 symbols";
    public static final String INVALID_PASSWORD_SPECIAL = "Password must contain special symbols";
    public static final String INVALID_PASSWORD_UPPERCASE = "Password must contain uppercase letters";
}
