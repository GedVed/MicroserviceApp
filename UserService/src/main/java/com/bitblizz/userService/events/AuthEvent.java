package com.bitblizz.userService.events;


import com.bitblizz.userService.rabbit.UserPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthEvent implements Serializable {
    private UserPayload userPayload;
    private String eventType;
}
