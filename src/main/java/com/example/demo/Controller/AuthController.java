//package com.example.demo.Controller;
//
//import com.example.demo.Repository.UserRepository;
//import com.example.demo.config.JwtUtil;
//import com.example.demo.models.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//    @Autowired // Add this!
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    // A simple DTO class inside the controller for login data
//    public static class LoginRequest {
//        public String username;
//        public String password;
//    }
//
////    @PostMapping("/register")
////    public ResponseEntity<?> register(@RequestBody User user) {
////        // 1. Encrypt the password
////        user.setPassword(passwordEncoder.encode(user.getPassword()));
////
////        // 2. Default role if none provided
////        if (user.getRole() == null) user.setRole("VENDOR");
////
////        // 3. Save to MySQL
////        userRepository.save(user);
////
////        return ResponseEntity.ok("User registered successfully!");
////    }
////    @PostMapping("/login")
////    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
////        // 1. Fetch user from MySQL
////        User user = userRepository.findByUsername(request.username);
////
////        // 2. Verify user exists and BCrypt password matches
////        if (user != null && passwordEncoder.matches(request.password, user.getPassword())) {
////
////            // 3. Generate token using the REAL role from the database
////            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
////
////            return ResponseEntity.ok(Map.of(
////                    "token", token,
////                    "role", user.getRole()
////            ));
////        }
////
////        return ResponseEntity.status(401).body("Invalid username or password");
////    }}
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody User user) {
//        System.out.println("--- REGISTER ATTEMPT ---");
//        System.out.println("Username: " + user.getUsername());
//
//        // Encrypt the password before saving
//        String encoded = passwordEncoder.encode(user.getPassword());
//        user.setPassword(encoded);
//
//        if (user.getRole() == null) user.setRole("VENDOR");
//
//        userRepository.save(user);
//        System.out.println("User saved successfully with hashed password!");
//        return ResponseEntity.ok("User registered successfully!");
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//
//        System.out.println("--- LOGIN ATTEMPT ---");
//        System.out.println("Login for: " + request.username);
//
//        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
//        if (user == null) {
//            System.out.println("Error: User not found in MySQL database!");
//            return ResponseEntity.status(401).body("Invalid username");
//        }
//
//        // This is the part that usually fails if the hash is wrong
//        boolean isMatch = passwordEncoder.matches(request.password, user.getPassword());
//        System.out.println("Password Match: " + isMatch);
//
//        if (isMatch) {
//            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
//            System.out.println("Login Successful! Token generated.");
//            return ResponseEntity.ok(Map.of("token", token, "role", user.getRole()));
//        }
//
//        System.out.println("Error: Password does not match the hash in database!");
//        return ResponseEntity.status(401).body("Invalid password");
//    }}

package com.example.demo.Controller;

import com.example.demo.Repository.UserRepository;
import com.example.demo.config.JwtUtil;
import com.example.demo.models.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Ye line add karein
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Fixed: Added Getters so request.getUsername() works
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        System.out.println("--- REGISTER ATTEMPT ---");

        // 1. Encrypt the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. Ensure role has ROLE_ prefix for Spring Security hasRole() to work
        if (user.getRole() == null) {
            user.setRole("ROLE_VENDOR");
        } else if (!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole().toUpperCase());
        }

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("--- LOGIN ATTEMPT ---");

        // Use the Repository (Returning Optional<User>)
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("Invalid username");
        }

        // Check password against BCrypt hash
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", user.getRole()
            ));
        }

        return ResponseEntity.status(401).body("Invalid password");
    }
    @Autowired
    private com.example.demo.Service.TokenBlacklistService blacklistService;

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        System.out.println("Logout endpoint hit!"); // <--- ADD THIS
        String authHeader = request.getHeader("Authorization");
        System.out.println("Header received: " + authHeader); // <--- ADD THIS
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            blacklistService.blacklistToken(jwt);
            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid logout request");
    }
}
