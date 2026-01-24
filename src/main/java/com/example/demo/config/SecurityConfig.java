//////
////////SecurityConfig (The Rulebook)
////////This is the central configuration class where you define the security "policies" for your entire application.
//////
//////package com.example.demo.config;
//////
//////import org.springframework.beans.factory.annotation.Autowired;
//////import org.springframework.context.annotation.Bean;
//////import org.springframework.context.annotation.Configuration;
//////import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//////import org.springframework.security.config.http.SessionCreationPolicy;
//////import org.springframework.security.core.userdetails.UserDetailsService;
//////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//////import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//////import org.springframework.security.web.SecurityFilterChain;
//////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//////
//////@Configuration
//////@EnableWebSecurity
//////@EnableMethodSecurity
//////public class SecurityConfig {
//////    // ADD THE @BEAN ANNOTATION HERE
//////    @Bean
//////    public BCryptPasswordEncoder passwordEncoder() {
//////        return new BCryptPasswordEncoder();
//////    }
//////    @Autowired
//////    private JwtFilter jwtFilter;
//////
//////
//////
////////    @Bean
////////    public UserDetailsService userDetailsService() {
////////        return new InMemoryUserDetailsManager();
////////    }
////////
////////    // FIX: Added @Bean here so Spring actually uses this filter chain
////////
////////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////////        http.csrf(csrf -> csrf.disable())
////////                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
////////                .authorizeHttpRequests(auth -> auth
////////                        // 1. Public: Login endpoint
////////                        .requestMatchers("/api/auth/**").permitAll()
////////
////////                        // 2. Admin-only: User Management
////////                        .requestMatchers("/api/users/**").hasRole("ADMIN")
////////
////////                        // 3. Finance & Admin
////////                        .requestMatchers("/api/pr/**", "/api/po/**").hasAnyRole("ADMIN", "FINANCE")
////////
////////                        // 4. All roles
////////                        .requestMatchers("/api/vendors/**").hasAnyRole("ADMIN", "FINANCE", "VENDOR")
////////
////////                        .anyRequest().authenticated()
////////                )
////////                // This makes sure our JWT filter runs before the login filter
////////                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
////////
////////        return http.build();
////////    }
////////}
//////
//////
//////    // YOU MUST ADD THIS ANNOTATION
//////    @Bean
//////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//////        http.csrf(csrf -> csrf.disable())
//////                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//////                .authorizeHttpRequests(auth -> auth
//////                        // This will finally work once the @Bean is added!
//////                        .requestMatchers("/api/auth/**").permitAll()
//////                        .requestMatchers("/api/users/**").hasRole("ADMIN")
//////                        .requestMatchers("/api/pr/**", "/api/po/**").hasAnyRole("ADMIN", "FINANCE")
//////                        .requestMatchers("/api/vendors/**").hasAnyRole("ADMIN", "FINANCE", "VENDOR")
//////                        .anyRequest().authenticated()
//////                )
//////                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//////
//////        return http.build();
//////    }}
//////
//////package com.example.demo.config;
//////
//////import org.springframework.context.annotation.Bean;
//////import org.springframework.context.annotation.Configuration;
//////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//////import org.springframework.security.web.SecurityFilterChain;
//////
//////@Configuration
//////@EnableWebSecurity
//////public class SecurityConfig {
//////
//////    // This is the bean your AuthController is looking for!
//////    @Bean
//////    public BCryptPasswordEncoder passwordEncoder() {
//////        return new BCryptPasswordEncoder();
//////    }
//////
//////    @Bean
//////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//////        http
//////                .csrf(csrf -> csrf.disable()) // Disable CSRF for development/Postman
//////                .authorizeHttpRequests(auth -> auth
//////                        .requestMatchers("/api/auth/**").permitAll() // Allow auth endpoints
//////                        .anyRequest().permitAll() // Temporarily permit all to test Postman
//////                );
//////
//////        return http.build();
//////    }
//////}
//////
//////package com.example.demo.config;
//////
//////import org.springframework.context.annotation.Bean;
//////import org.springframework.context.annotation.Configuration;
//////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//////import org.springframework.security.web.SecurityFilterChain;
//////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//////import org.springframework.security.crypto.password.PasswordEncoder;
//////
//////@Configuration
//////public class SecurityConfig {
//////
//////    @Bean
//////    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//////
//////        http
//////                // Disable CSRF for APIs
//////                .csrf(csrf -> csrf.disable())
//////
//////                // Allow APIs without login (for now)
//////                .authorizeHttpRequests(auth -> auth
//////                        .requestMatchers(
//////                                "/auth/**",
//////                                "/login",
//////                                "/register",
//////                                "/swagger-ui/**",
//////                                "/v3/api-docs/**"
//////                        ).permitAll()
//////                        .anyRequest().authenticated()
//////                )
//////
//////                // Disable default login page & basic auth
//////                .formLogin(form -> form.disable())
//////                .httpBasic(basic -> basic.disable());
//////
//////        return http.build();
//////    }
//////
//////    @Bean
//////    public PasswordEncoder passwordEncoder() {
//////        return new BCryptPasswordEncoder();
//////    }
//////}
//////
//////package com.example.demo.config;
//////
//////import org.springframework.context.annotation.Bean;
//////import org.springframework.context.annotation.Configuration;
//////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//////import org.springframework.security.web.SecurityFilterChain;
//////
//////@Configuration
//////@EnableWebSecurity
//////public class SecurityConfig {
//////
//////    @Bean
//////    public BCryptPasswordEncoder passwordEncoder() {
//////        return new BCryptPasswordEncoder();
//////    }
//////
//////    @Bean
//////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//////        http
//////                .csrf(csrf -> csrf.disable())
//////                .authorizeHttpRequests(auth -> auth
//////                        // 1. Public Endpoints (Registration/Login)
//////                        .requestMatchers("/api/auth/**").permitAll()
//////
//////                        // 2. Admin-only Endpoints
//////                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//////
//////                        // 3. Finance-only Endpoints
//////                        .requestMatchers("/api/finance/**").hasRole("FINANCE")
//////
//////                        // 4. Vendor-only Endpoints
//////                        .requestMatchers("/api/vendor/**").hasRole("VENDOR")
//////
//////                        // 5. Everything else requires at least a login
//////                        .anyRequest().authenticated()
//////                )
//////
//////                .logout(logout -> logout
//////                        .logoutUrl("/api/auth/logout")
//////                        .logoutSuccessHandler((request, response, authentication) -> {
//////                            response.setStatus(200); // Send OK status on logout
//////                        })
//////                );
//////
//////        return http.build();
//////    }
//////}
////
////
////package com.example.demo.config;
////// it defines which URLs are public (like /login or /register) and which ones require a valid token. It also tells the system to use the JwtRequestFilter mentioned above.
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
////import org.springframework.security.config.http.SessionCreationPolicy;
////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
////
////@Configuration
////@EnableWebSecurity
////public class SecurityConfig {
////
////    @Autowired
////    private JwtRequestFilter jwtRequestFilter;
////
////    @Bean
////    public BCryptPasswordEncoder passwordEncoder() {
////        return new BCryptPasswordEncoder();
////    }
////
////    @Bean
////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////        http
////                .csrf(csrf -> csrf.disable()) // Crucial for Postman POST requests
////                .authorizeHttpRequests(auth -> auth
////                        // 1. Everyone can register or login
////                        .requestMatchers("/api/auth/**").permitAll()
////
////                        // 2. Role-Based Access Control (RBAC)
////                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
////                        .requestMatchers("/api/finance/**").hasRole("FINANCE")
////                        .requestMatchers("/api/vendor/**").hasRole("VENDOR")
////
////                        // 3. Secure all other endpoints
////                        .anyRequest().authenticated()
////                )
////                .sessionManagement(session -> session
////                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
////                )
////                .sessionManagement(session -> session
////                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
////                )
////                // 5. Add your custom JWT Filter before the standard UsernamePasswordAuthenticationFilter
////                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
////
////
////        return http.build();
////    }











//SecurityConfig (The Rulebook)
//This is the central configuration class where you define the security "policies" for your entire application.

package com.example.demo.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

                    // FIX: Added "PATCH" to the allowed methods list below
                    // This was missing and causing the net::ERR_FAILED in your browser
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

                    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

                    // Optional: Allow credentials if you plan to use cookies later
                    config.setAllowCredentials(true);
                    return config;
                }))

                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/logout").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/update/**").authenticated()
                        .requestMatchers("/api/pr/**").authenticated()
                        .requestMatchers("/api/po/convert/**").hasAnyRole("ADMIN", "FINANCE")
                        .requestMatchers("/api/po/items/**").hasAnyRole("ADMIN", "VENDOR")
                        .requestMatchers("/api/po/**").authenticated()
                        .requestMatchers("/api/deliveries/**").hasAnyRole("ADMIN", "VENDOR")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/finance/**").hasAnyRole("ADMIN", "FINANCE")
                        .requestMatchers("/api/vendor/**").hasAnyRole("ADMIN", "VENDOR")
                        .requestMatchers("/api/reports/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Access Denied\", \"message\": \"You do not have the required role to access this module.\"}");
                        })
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}


// updated security config

//
//package com.example.demo.config;
//
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//
///**
// * WORKING EXPLANATION:
// * 1. corsConfigurationSource: Ye method explicit taur par "PATCH" aur "OPTIONS"
// * methods ko allow karta hai, jo ki Deactivate button ke CORS issue ko fix karega.
// * 2. securityFilterChain: Aapke saare existing role-based rules yahan consolidated hain.
// * 3. JWT Integration: JwtRequestFilter ko filter chain mein add kiya gaya hai
// * taaki token validate ho sake.
// */
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private JwtRequestFilter jwtRequestFilter;
//
//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // 1. Enable CORS with our custom configuration
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//
//                // 2. Disable CSRF for Stateless API
//                .csrf(csrf -> csrf.disable())
//
//                // 3. Authorization Rules (Consolidated from your shared code)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/logout").authenticated()
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/users/update/**").authenticated()
//                        .requestMatchers("/api/pr/**").authenticated()
//                        .requestMatchers("/api/po/convert/**").hasAnyRole("ADMIN", "FINANCE")
//                        .requestMatchers("/api/po/items/**").hasAnyRole("ADMIN", "VENDOR")
//                        .requestMatchers("/api/po/**").authenticated()
//                        .requestMatchers("/api/deliveries/**").hasAnyRole("ADMIN", "MANAGER")
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/finance/**").hasAnyRole("ADMIN", "FINANCE")
//                        .requestMatchers("/api/vendor/**").hasAnyRole("ADMIN", "VENDOR")
//                        .requestMatchers("/api/reports/**").permitAll()
//                        .requestMatchers("/error").permitAll()
//                        .anyRequest().authenticated()
//                )
//
//                // 4. Custom Error Handling (403 Forbidden)
//                .exceptionHandling(ex -> ex
//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                            response.setContentType("application/json");
//                            response.getWriter().write("{\"error\": \"Access Denied\", \"message\": \"Insufficient Permissions\"}");
//                        })
//                )
//
//                // 5. Stateless Session Management
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//
//                // 6. JWT Filter
//                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    /**
//     * METHOD: corsConfigurationSource
//     * Working: Ye backend ko batata hai ki localhost:3000 se aane wali
//     * PATCH requests (toggle-status) ko block na kare.
//     */
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
//        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
//}