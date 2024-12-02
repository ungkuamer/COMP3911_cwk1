package main.java.comp3911.cwk2;

import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int MAX_ATTEMPTS = 5; // Maximum inalid attempts
    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> blockedUsers = new ConcurrentHashMap<>();

    // Check if a username is allowed
    public boolean isAllowed(String username) {
        // Check if the username is permanently blocked
        return !blockedUsers.getOrDefault(username, false);
    }

    // Log failed log in attemp
    public void recordFailure(String username) {
        if (blockedUsers.getOrDefault(username, false)) {
            return;
        }

        int failedAttempts = attempts.getOrDefault(username, 0) + 1;
        attempts.put(username, failedAttempts);

        if (failedAttempts >= MAX_ATTEMPTS) {
            blockedUsers.put(username, true); // Permanently block the username
            attempts.remove(username); // Clean up
        }
    }

    // Check if user is blocked
    public boolean isBlocked(String username) {
        return blockedUsers.getOrDefault(username, false);
    }
    
    // Reset attempts after authenticated
    public void resetAttempts(String username) {
        if (blockedUsers.getOrDefault(username, false)) {
            return; // No reset if blocked
        }

        attempts.remove(username); // Reset count
    }


}
