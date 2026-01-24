package com.example.demo.Repository;

import com.example.demo.models.Role;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // <--- Make sure this is imported

/**
 * Data Access Layer for User entities.
 * This interface facilitates database operations such as finding users by
 * username, filtering by roles, and counting specific user types.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a list of users based on their assigned role string.
     * Spring generates: SELECT * FROM users WHERE username = ?
     * @param role The role string to filter by (e.g., "ROLE_VENDOR").
     * @return A list of users matching the specified role.
     */
    List<User> findByRole(String role);

    /**
     * Finds a specific user by their unique username.
     * @param username The username to search for.
     * @return An Optional containing the User if found, or empty if not.
     */
    Optional<User> findByUsername(String username);

    /**
     * Counts the total number of users associated with a specific role.
     * @param role The role string to count.
     * @return The total number of users belonging to that role.
     */
    long countByRole(String role);
}