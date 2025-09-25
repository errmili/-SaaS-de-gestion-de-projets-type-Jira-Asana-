// ===========================================
// CustomUserDetailsService.java - Service UserDetails
// ===========================================
package com.projectsaas.auth.security;

import com.projectsaas.auth.entity.User;
import com.projectsaas.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Le username contient email@subdomain
        String[] parts = username.split("@");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Invalid username format");
        }

        String email = parts[0];
        String subdomain = parts[1];

        User user = userRepository.findByEmailAndTenantSubdomain(email, subdomain)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return user; // User implémente UserDetails
    }
}