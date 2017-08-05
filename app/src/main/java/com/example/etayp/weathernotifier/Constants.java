package com.example.etayp.weathernotifier;

/**
 * Created by EtayP on 31-Jul-17.
 */

public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final int WHOLE_ADRESS = 0;
    public static final int CITY_ONLY = 1;

    public static final String PACKAGE_NAME =
            "com.example.etayp.weathernotifier";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    public static final String ADDRESS_TYPE_EXTRA = PACKAGE_NAME +
            ".ADDRESS_TYPE_EXTRA";

    public static final long LOCATION_UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    public static final long LOCATION_FASTEST_INTERVAL = 2000; /* 2 sec */
    public static final long MIN_TIME_BW_UPDATES = 60 * 1000;
    public static final float MIN_DISTANCE_CHANGE_FOR_UPDATES=30;
}
