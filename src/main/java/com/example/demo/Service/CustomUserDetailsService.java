package com.example.demo.Service;

// THESE IMPORTS FIX THE "CANNOT RESOLVE" ERRORS
import com.example.demo.models.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Service implementation for Spring Security's UserDetailsService.
 * This class bridge the gap between your database (UserRepository)
 * and Spring Security's authentication mechanism.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor-based injection for the UserRepository.
     * @param userRepository the database repository for user data.
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user based on the username provided during login.
     * * @param username the username identifying the user whose data is required.
     * @return a fully populated UserDetails object for Spring Security to use.
     * @throws UsernameNotFoundException if the user could not be found in the database.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Normalizing the role format to ensure consistency
        String dbRole = user.getRole().toUpperCase();
        String finalRole = dbRole.startsWith("ROLE_") ? dbRole : "ROLE_" + dbRole;

        // Return a Spring Security User object
        // This maps your custom User entity to the Security framework's standard UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername()) // Fixed typo 'getame' to 'getUsername'
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole())
                .build();
    }
}