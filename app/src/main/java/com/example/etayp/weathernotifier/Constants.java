package com.example.etayp.weathernotifier;

/**
 * Created by EtayP on 31-Jul-17.
 */

final class Constants {
    static final int SUCCESS_RESULT = 0;
    static final int FAILURE_RESULT = 1;
    static final int WHOLE_ADDRESS = 0;
    static final int CITY_ONLY = 1;
    static final int RECEIVE_TO_MAIN = 0;
    static final int RECEIVE_TO_FRAGMENT = 1;


    private static final String PACKAGE_NAME =
            "com.example.etayp.weathernotifier";
    static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    static final String ADDRESS_TYPE_EXTRA = PACKAGE_NAME +
            ".ADDRESS_TYPE_EXTRA";
    static final String RECEIVE_TYPE_EXTRA = PACKAGE_NAME +
            ".RECEIVE_TYPE_EXTRA";

}
