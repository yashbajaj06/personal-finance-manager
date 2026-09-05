package com.finance.manager.security;

import com.finance.manager.entity.User;
import com.finance.manager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_Success() {
        User user = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("encoded-password")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .build();

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("test@example.com");

        assertEquals("test@example.com", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_NotFoundThrows() {
        when(userRepository.findByUsername("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@example.com"));
    }
}
