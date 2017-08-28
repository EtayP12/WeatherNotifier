package com.example.etayp.weathernotifier;

import android.content.SharedPreferences;

/**
 * Created by EtayP on 31-Jul-17.
 */

final class Constants {
    //codes
    static final int SUCCESS_RESULT = 0;
    static final int FAILURE_RESULT = 1;
    static final int RECEIVE_TO_MAIN = 0;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    static final int RECEIVE_TO_FRAGMENT = 1;

    private static final String PACKAGE_NAME =
            "com.example.etayp.weathernotifier";
    static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    static final String RECEIVE_TYPE_EXTRA = PACKAGE_NAME +
            ".RECEIVE_TYPE_EXTRA";


    //Api keys
    public final String API_KEY1 = "4e687457bbdb40a25dd4a30b8d92ec0c";
    static final String API_KEY2 = "ba5b4ae760ee1d74eea0e5d70514cdf4";

    //SharedPreferences keys
    static final String UPDATE_TIME_SELECTION = "update time";
    static final String ADDRESSES_PREFERENCE = "addresses preference";
    static final String SET_ALARM_TIME = "Set alarm time";
    static final String ALARM_HOUR = "alarm hour";
    static final String ALARM_MINUTE = "alarm minute";
    static final String ALARM_IS_ACTIVE = "alarm is active";
    static final String WEATHER_UPDATE_ITEMS = "weather update items";
    static final String ADDRESSES_HASH_MAP = "addresses hash map";
    static final String NUMBER_OF_ADDRESSES = "number of addresses";
    static final String OPTION_TEMPRATURE = "option_temprature";
    static final String OPTION_WIND = "option_wind";
    static final String OPTION_RAIN = "option_rain";
    static final String OPTION_HUMIDITY = "option_humidity";
    static final String DAILY_WEATHER_ITEMS = "daily weather items";
    static final String NEW_LOCATION = "NewLocation";
    static final String EXIT_WITH_BACK_BUTTON = "exit with back button";

    //Strings
    static final String APPLICATION_NEEDS_PERMISSION = "Application needs permission";
    static final String NO_OPTION_SELECTED_TITLE = "No option selected";
    static final String NO_LOCATION_SELECTED = "No location selected";
    static final String NO_OPTION_SELECTED_TEXT = "If you don't select any option, Weather Notifier wont sent you notifications";
    static final String GOT_IT = "Got it";
    static final String GO_BACK = "Go back";
    static final String DEGREE = "°";
    static final String PERCENT = "%";
}
