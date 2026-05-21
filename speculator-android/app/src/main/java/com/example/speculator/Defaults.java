package com.example.speculator;


import static android.util.TypedValue.COMPLEX_UNIT_DIP;

import android.util.TypedValue;

import java.time.Duration;
import java.time.ZonedDateTime;

public class Defaults {
    public static Duration appCycleInterval = Duration.ofSeconds(2);
    public static ZonedDateTime backtestAt = ZonedDateTime.now();
    public static float formRowHeight = 35;
    public static int actionListLimit = 10;
    public static Duration simPeriod = Duration.ofHours(1);
    public static Duration simPastInterval = Duration.ofMinutes(3);

}
