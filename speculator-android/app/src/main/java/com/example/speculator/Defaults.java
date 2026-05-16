package com.example.speculator;

import java.time.Duration;
import java.time.ZonedDateTime;

public class Defaults {
    public static Duration appCycleInterval = Duration.ofSeconds(2);
    public static ZonedDateTime backtestAt = ZonedDateTime.now();
}
