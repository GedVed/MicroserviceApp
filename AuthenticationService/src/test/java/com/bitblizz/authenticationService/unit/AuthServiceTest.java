package com.bitblizz.authenticationService.unit;


import com.bitblizz.authenticationService.model.User;
import com.bitblizz.authenticationService.events.AuthenticationEvent;
import com.bitblizz.authenticationService.rabbit.RabbitSender;
import com.bitblizz.authenticationService.rabbit.UserPayload;
import com.bitblizz.authenticationService.repository.UserRepository;
import com.bitblizz.authenticationService.service.AuthenticationService;
import com.bitblizz.authenticationService.service.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private AuthenticationService authenticationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RabbitSender rabbitSender;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthenticationService(userRepository,jwtService,rabbitSender,passwordEncoder);
    }


    @Test
    void testSaveUser(){
        User user = new User("John", "Doe", "testuser","testpassword");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        String added = authenticationService.saveUser(user);
        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testErrorAddingUser() {
        User user = new User("John", "Doe", "testuser","testpassword");

        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        String result = authenticationService.saveUser(user);

        assertEquals("Username taken", result);
        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRabbitSender(){
        User user = new User("John", "Doe", "testuser","testpassword");
        user.setId(UUID.randomUUID());

        when(userRepository.save(any(User.class))).thenReturn(user);
        authenticationService.saveUser(user);
        UserPayload payload = new UserPayload(user.getId().toString(),user.getFirstname(),user.getLastname(), user.getUsername());
        AuthenticationEvent event = new AuthenticationEvent("create",payload);
        verify(rabbitSender,times(1)).sendMessage("auth.exchange","auth.create", event);
    }

    @Test
    void testValidateToken(){
        String token = "testtoken";
        Mockito.doThrow(new JwtException("Invalid token")).when(jwtService).validateToken(token);

        assertThrows(JwtException.class, () -> authenticationService.validateToken(token));
    }
}
