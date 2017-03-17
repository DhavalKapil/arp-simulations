package com.secarp.common;

/**
 * Utility class for managing time related operations
 */
public class Timer {

    /**
     * Returns the current time in seconds
     *
     * @return The current unix timestamp in seconds
     */
    public static int getCurrentTime() {
        return (int)(System.currentTimeMillis()/1000);
    }

    /**
     * Sleeps for a particular amount of time
     *
     * @param millis The number of milliseconds to sleep for
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {
            // You're doomed!
        }
    }
}
