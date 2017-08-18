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
}
