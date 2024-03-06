package com.bitblizz.authenticationService.request;


import com.bitblizz.authenticationService.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String token;
    private String refreshToken;
    private String username;
    private String firstname;
    private String lastname;
    private List<Role> userRole;
}
