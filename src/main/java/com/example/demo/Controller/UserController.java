//package com.example.demo.Controller;
//
//
//
//import com.example.demo.dto.UserUpdateDTO;
//
//import com.example.demo.models.Role;
//
//import com.example.demo.models.User;
//
//import com.example.demo.Repository.UserRepository;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.http.HttpStatus;
//
//import org.springframework.http.ResponseEntity;
//
//import org.springframework.web.bind.annotation.*;
//
//
//
//import java.util.List;
//
//
//
//
//@RestController
//
//@RequestMapping("/api/users")
//
//@CrossOrigin(origins = "http://localhost:3000") // React URL
//
//public class UserController {
//
//
//
//    @Autowired
//
//    private UserRepository userRepository;
//
//
//
//    @PutMapping("/update/{username}")
//
//    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody UserUpdateDTO updateRequest) {
//
//        return userRepository.findByUsername(username)
//
//                .map(user -> {
//
//// Sirf email aur mobile update kar rahe hain
//
//                    user.setEmail(updateRequest.getEmail());
//
//                    user.setMobileNo(updateRequest.getMobileNo());
//
//                    user.setCompanyName(updateRequest.getCompanyName());
//
//                    user.setAddress(updateRequest.getAddress());
//
//
//                    userRepository.save(user);
//
//                    return ResponseEntity.ok("Profile updated successfully!");
//
//                })
//
//                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
//
//    }
//
//
//
//    @GetMapping("/role/{roleName}")
//    public ResponseEntity<?> getUsersByRole(@PathVariable String roleName) {
//        try {
//            // METHOD EXPLANATION:
//            // 1. Aapki User Entity mein 'role' field String hai.
//            // 2. Aapki UserRepository findByRole(String role) accept karti hai.
//            // 3. Isliye hum Enum (Role.valueOf) wala logic hata kar direct String use karenge.
//
//            String roleToSearch = "ROLE_" + roleName.toUpperCase(); // e.g., "vendor" -> "VENDOR"
//
//            // 4. 'role' variable ka naam sahi kiya (pehle wahan 'roleEnum' tha aur niche 'role')
//            List<User> users = userRepository.findByRole(roleToSearch);
//
//            return ResponseEntity.ok(users);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error: " + e.getMessage());
//        }
//    }
//
//// METHOD EXPLANATION:
//// 1. Humne explicitly return type ResponseEntity<?> rakha hai.
//// 2. userRepository.findByUsername ek Optional return karta hai.
//// 3. .map(user -> ...) : Agar user mil gaya, toh hum ResponseEntity.ok(user) return kar rahe hain.
//// 4. .orElse(...) : Agar user NULL hai (nahi mila), toh hum explicitly 404 status ke saath String message bhej rahe hain.
//// Is approach mein inference error nahi aayega kyunki return types clear hain.
//
//    @GetMapping("/{username}")
//    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
//        User user = userRepository.findByUsername(username).orElse(null);
//
//        if (user != null) {
//            return ResponseEntity.ok(user); // Success: returns User object
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with username " + username + " not found"); // Error: returns String
//        }
//    }
//
//
//
//}









package com.example.demo.Controller;

import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.models.Role;
import com.example.demo.models.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class to handle all user-related HTTP requests.
 * Provides endpoints for updating user profiles and retrieving user data.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000") // React URL
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Updates specific profile fields for a user identified by their username.
     * * @param username the unique identifier of the user.
     * @param updateRequest the data transfer object containing new profile details.
     * @return a success message if the user is found and updated; otherwise, a 404 status.
     */
    @PutMapping("/update/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody UserUpdateDTO updateRequest) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    // Only updating specific fields: email, mobile, company name, and address
                    user.setEmail(updateRequest.getEmail());
                    user.setMobileNo(updateRequest.getMobileNo());
                    user.setCompanyName(updateRequest.getCompanyName());
                    user.setAddress(updateRequest.getAddress());

                    userRepository.save(user);
                    return ResponseEntity.ok("Profile updated successfully!");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    /**
     * Retrieves a list of users filtered by their assigned role.
     * * @param roleName the name of the role to search for (e.g., "admin", "vendor").
     * @return a list of users matching the role or an error response in case of an exception.
     */
    @GetMapping("/role/{roleName}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String roleName) {
        try {
            // METHOD EXPLANATION:
            // 1. The 'role' field in the User Entity is a String.
            // 2. The UserRepository.findByRole(String role) method accepts a String.
            // 3. Instead of Enum logic, we use direct String manipulation.

            // Formatting the input: e.g., "vendor" becomes "ROLE_VENDOR"
            String roleToSearch = "ROLE_" + roleName.toUpperCase();

            // Fetching users from the repository based on the formatted role string
            List<User> users = userRepository.findByRole(roleToSearch);

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Fetches detailed information for a specific user.
     * * @param username the username of the user to search for.
     * @return the User object if found, or a 404 status message if not found.
     */
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        // Fetch user from repository; returns null if not present
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {
            // Returns the full User object for a successful match
            return ResponseEntity.ok(user);
        } else {
            // Returns a detailed error message if no user is found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with username " + username + " not found");
        }
    }
}




















