package com.frameasy.security;

import com.frameasy.model.User;
import com.frameasy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Load user by email (login) or by userId string (from JWT subject).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        if (username.matches("\\d+")) {
            user = userRepository.findById(Long.parseLong(username))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } else {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }
        return UserPrincipal.create(user);
    }
}
