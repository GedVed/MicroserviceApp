package com.bitblizz.authenticationService.events;


import com.bitblizz.authenticationService.rabbit.UserPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationEvent {
    private String eventType;
    private UserPayload userPayload;
}
