package com.bitblizz.authenticationService.rabbit;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPayload {
    private String userId;
    private String firstname;
    private String lastname;
    private String username;
}
