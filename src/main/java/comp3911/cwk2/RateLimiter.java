package main.java.comp3911.cwk2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RateLimiter {
    private static final int MAXIMUM_REQUESTS = 5; // Maximum requests allowed
    private static final long TIME_WINDOW = 60_000; // Time window in milliseconds

    // Map to track the last request times for each user
    private final Map<String, long[]> userRequestLog = new HashMap<>();
    private final Set<String> blockedUser = new HashSet<>();

    public boolean isAllowed(String key) {

        if(blockedUser.contains(key)){
            System.out.println("User is blocked. Please contact IT Services");
            return false;
        }

        long now = System.currentTimeMillis();

        // Ensure the user has a request log initialized
        userRequestLog.putIfAbsent(key, new long[MAXIMUM_REQUESTS]); // Initialize with default values
        
        // Get the request log for the user
        long[] timestamps = userRequestLog.get(key);

        // Count valid requests in the time window
        int validRequestCount = 0;
        for (long timestamp : timestamps) {
            if (now - timestamp <= TIME_WINDOW) {
                validRequestCount++;
            }
        }

        // Check if the user can make another request
        if (validRequestCount < MAXIMUM_REQUESTS) {
            // Add the current request to the log
            for (int i = 0; i < timestamps.length; i++) {
                if (now - timestamps[i] > TIME_WINDOW) { // Replace the oldest/expired timestamp
                    timestamps[i] = now;
                    break;
                }
            }
            return true; // Request is allowed
        }
        System.out.println("Login limit exceeded. User is block");
        blockedUser.add(key);
        return false; // Rate limit exceeded
    }
}
