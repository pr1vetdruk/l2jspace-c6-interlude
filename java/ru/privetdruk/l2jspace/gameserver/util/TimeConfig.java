package ru.privetdruk.l2jspace.gameserver.util;

/**
 * Hold number of milliseconds for wide-using time periods.
 *
 * @author GKR
 * Modified by pr1vetdruk
 */
public enum TimeConfig {
    NONE(-1L, "", ""),
    SECOND(1000L, "second", "s"),
    MINUTE(60000L, "minute", "m"),
    HOUR(3600000L, "hour", "h"),
    DAY(86400000L, "day", "d"),
    WEEK(604800000L, "week", "w"),
    MONTH(2592000000L, "Month", "M");

    /**
     * Count of milliseconds
     */
    private final long millis;

    /**
     * Mnemonic name of period
     */
    private final String name;

    /**
     * Short name of period
     */
    private final String shortName;

    TimeConfig(long millis, String name, String shortName) {
        this.millis = millis;
        this.name = name;
        this.shortName = shortName;
    }

    public long getTimeInMillis() {
        return millis;
    }


    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    /**
     * @param period supported tag to parse.Supported tags are "s" for seconds, "m" for minutes,
     *               "h" for hours, "d" for days, "w" for weeks, M - for conventional "month" (30 days).
     * @return TimeConstant object, corresponding to given tag, or TimeConstant.NONE for invalid tags.
     */
    public static TimeConfig getTimeConstant(String period) {
        for (TimeConfig tc : TimeConfig.values()) {
            if (tc.getShortName().equals(period)) {
                return tc;
            }
        }
        return TimeConfig.NONE;
    }

    /**
     * @param value string to parse. Format is "<number><supported tag>", supported tags are "s" for seconds, "m" for minutes,
     *                   "h" for hours, "d" for days, "w" for weeks, m - for conventional "month" (30 days). Valid value for example: "25s"
     * @return number of milliseconds in given period, or -1 if format of period is not valid
     */
    public static long parse(String value) {
        long millis = -1;

        if (value == null) {
            return millis;
        }

        String number = value.substring(0, value.length() - 1); // Whole string, except last symbol
        String period = value.substring(value.length() - 1); // assume, that last symbol is code of time period

        TimeConfig tc = getTimeConstant(period);
        if (tc != TimeConfig.NONE) {
            try {
                int num = Integer.parseInt(number);
                millis = tc.getTimeInMillis() * num;
            } catch (NumberFormatException nfe) {
                // Do nothing
            }
        }

        return millis;
    }
}
