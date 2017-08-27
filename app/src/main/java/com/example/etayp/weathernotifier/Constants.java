package com.example.etayp.weathernotifier;

/**
 * Created by EtayP on 31-Jul-17.
 */

final class Constants {
    static final int SUCCESS_RESULT = 0;
    static final int FAILURE_RESULT = 1;
    static final int RECEIVE_TO_MAIN = 0;
    static final int RECEIVE_TO_FRAGMENT = 1;


    private static final String PACKAGE_NAME =
            "com.example.etayp.weathernotifier";
    static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    static final String RECEIVE_TYPE_EXTRA = PACKAGE_NAME +
            ".RECEIVE_TYPE_EXTRA";

    public static final String UPDATE_TIME_SELECTION = "update time";
    public static final String ALERT_DIALOG_TITLE = "Set update time";
    public static final String NUMBER_OF_ADDRESSES = "number of addresses";
    public static final String ADDRESSES_PREFERENCE = "addresses preference";
    public static final String API_KEY1 = "4e687457bbdb40a25dd4a30b8d92ec0c";
    public static final String API_KEY2 = "ba5b4ae760ee1d74eea0e5d70514cdf4";
    public static final String ADDRESSES_HASH_MAP = "addresses hash map";
    public static final String SET_ALARM_TIME = "Set alarm time";
    public static final String ALARM_HOUR = "alarm hour";
    public static final String ALARM_MINUTE = "alarm minute";
    public static final String ALARM_IS_ACTIVE = "alarm is active";
    public static final String DEGREE = "°";
    public static final String PERCENT = "%";
    public static final String WEATHER_UPDATE_ITEMS = "weather update items";

    public static final String OPTION_TEMPRATURE = "option_temprature";
    public static final String OPTION_WIND = "option_wind";
    public static final String OPTION_RAIN = "option_rain";
    public static final String OPTION_HUMIDITY = "option_humidity";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    public static final String DAILY_WEATHER_ITEMS = "daily weather items";
}
