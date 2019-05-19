package com.rcarausu.ptdma.voiceassistant.services;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import com.rcarausu.ptdma.voiceassistant.utils.RequestCodes;

import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.WRITE_CALENDAR;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CalendarService {

    private static final CalendarService INSTANCE = new CalendarService();

    private CalendarService() {}

    // Declared as singleton
    public static CalendarService getInstance() {
        return INSTANCE;
    }

    public void grantCalendarPermissions(Activity activity) {
        if (!checkCalendarPermissions(activity)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{READ_CALENDAR, WRITE_CALENDAR},
                    RequestCodes.REQUEST_CALENDAR_PERMISSIONS_CODE);
        }
    }

    public boolean checkCalendarPermissions(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, READ_CALENDAR) == PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, WRITE_CALENDAR) == PERMISSION_GRANTED;
    }
}
