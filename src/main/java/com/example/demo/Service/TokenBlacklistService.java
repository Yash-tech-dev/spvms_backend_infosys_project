package com.example.demo.Service;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Service responsible for managing invalidated JWT tokens.
 * This is primarily used for "Logout" functionality, ensuring that
 * once a user logs out, their token cannot be reused even if it hasn't expired.
 */
@Service
public class TokenBlacklistService {

    /**
     * A thread-safe Set to store blacklisted tokens.
     * Working: Uses Collections.synchronizedSet to wrap a HashSet, allowing
     * multiple concurrent login/logout requests to access the set without data corruption.
     */
    private final Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());

    /**
     * Method: blacklistToken
     * Working: Adds a token string to the blacklist collection.
     * @param token The JWT string to be invalidated.
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    /**
     * Method: isBlacklisted
     * Working: Checks if a given token exists in the invalidation set.
     * @param token The JWT string to verify.
     * @return true if the token is blacklisted, false otherwise.
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}