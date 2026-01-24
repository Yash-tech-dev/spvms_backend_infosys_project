package com.example.demo.config;
// This is the Checkpoint that every incoming request must pass through before reaching your API.
import com.example.demo.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private com.example.demo.Service.TokenBlacklistService blacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 1. Extract Token and Check Blacklist IMMEDIATELY
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            // --- BLACKLIST CHECK MUST BE HERE ---
            if (blacklistService.isBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                response.getWriter().write("Error: This token has been logged out. Please login again.");
                return; // Stop the request immediately
            }

            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Handle cases where token is malformed or expired
                logger.error("Unable to extract username from token");
            }
        }

        // 2. Proceed with Authentication & Role Logic
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // Extract role and apply the "ROLE_" prefix logic
                String roleFromToken = jwtUtil.extractRole(jwt);
                String finalRole = roleFromToken.startsWith("ROLE_") ? roleFromToken : "ROLE_" + roleFromToken;

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(finalRole);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, Collections.singletonList(authority));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Finalize authentication
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue to the next filter in the chain
        chain.doFilter(request, response);
    }
}