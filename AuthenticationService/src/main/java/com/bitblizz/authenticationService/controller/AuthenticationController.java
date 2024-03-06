package com.bitblizz.authenticationService.controller;


import com.bitblizz.authenticationService.dto.DeleteDto;
import com.bitblizz.authenticationService.dto.UserDto;
import com.bitblizz.authenticationService.model.User;
import com.bitblizz.authenticationService.request.AuthenticationRequest;
import com.bitblizz.authenticationService.request.AuthenticationResponse;
import com.bitblizz.authenticationService.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/register")
    public String addUser(@RequestBody UserDto user){
        return authenticationService.saveUser(new User(user.getFirstname(),user.getLastname(),user.getUsername(),user.getPassword()));
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody AuthenticationRequest authenticationRequest){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        if (authentication.isAuthenticated()){
            return authenticationService.generateToken(authenticationRequest.getUsername());
        }else {
            throw new RuntimeException("No access");
        }
    }
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public boolean deleteUser(@RequestBody DeleteDto dto){
        return authenticationService.deleteUser(dto.getUsername(),dto.getPassword());
    }

    @PostMapping("/refresh/{refreshToken}")
    public AuthenticationResponse refreshToken(@PathVariable String refreshToken) {
        return authenticationService.refresh(refreshToken);
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token){
        authenticationService.validateToken(token);
        return "Valid token";
    }

}
