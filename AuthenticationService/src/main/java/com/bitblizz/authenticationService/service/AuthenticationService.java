package com.bitblizz.authenticationService.service;


import com.bitblizz.authenticationService.events.AuthenticationEvent;
import com.bitblizz.authenticationService.model.Role;
import com.bitblizz.authenticationService.model.User;
import com.bitblizz.authenticationService.rabbit.RabbitSender;
import com.bitblizz.authenticationService.rabbit.UserPayload;
import com.bitblizz.authenticationService.repository.UserRepository;

import com.bitblizz.authenticationService.request.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RabbitSender rabbitSender;

    private final PasswordEncoder passwordEncoder;


    public String saveUser(User user){
        boolean taken = userRepository.existsByUsername(user.getUsername());
        if(taken){
            return "Username taken";
        }else {

            user.setUserRole(List.of(Role.USER));
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User userCreate = userRepository.save(user);

            try {
                UserPayload userPayload = new UserPayload(userCreate.getId().toString(),userCreate.getFirstname(),userCreate.getLastname(),userCreate.getUsername());

                AuthenticationEvent event = new AuthenticationEvent("create", userPayload);
                rabbitSender.sendMessage("auth.exchange","auth.create", event);
            }catch (Exception e){
                return "Error while sending user to UserService";
            }
        }
        return "User has been added to the system";
    }


    public AuthenticationResponse generateToken(String username){
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            return null;
        }
        Map<String, String> tokens = jwtService.generateTokens(user.get(), username);
        return new AuthenticationResponse(tokens.get("accessToken"), tokens.get("refreshToken"), username, user.get().getFirstname(),
                user.get().getLastname(), user.get().getUserRole());
    }


    public AuthenticationResponse refresh(String refreshToken){

        String username = jwtService.getUsernameFromToken(refreshToken);
        return generateToken(username);
    }

    public boolean deleteUser(String username, String password){
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            return false;
        }

        if(passwordEncoder.matches(password, user.get().getPassword())){
            userRepository.delete(user.get());
            try {
                UserPayload userPayload = new UserPayload();
                userPayload.setUsername(user.get().getUsername());

                AuthenticationEvent authenticationEvent = new AuthenticationEvent("delete", userPayload);
                rabbitSender.sendMessage("auth.exchange", "auth.delete", authenticationEvent);
            }catch (Exception e){
                log.info("Cannot delete user");
            }
            return true;
        }else {
            return false;
        }
    }

    public void validateToken(String token){
        jwtService.validateToken(token);
    }


}
