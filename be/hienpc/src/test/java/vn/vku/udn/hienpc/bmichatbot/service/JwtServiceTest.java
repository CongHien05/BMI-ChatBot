package vn.vku.udn.hienpc.bmichatbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);

        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateTokenAndValidate() {
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.validateToken(token, userDetails));
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    @Test
    void validateToken_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = User.withUsername("other@example.com")
                .password("pwd")
                .authorities(Collections.emptyList())
                .build();

        assertFalse(jwtService.validateToken(token, otherUser));
    }
}


