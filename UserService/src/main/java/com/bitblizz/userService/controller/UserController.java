package com.bitblizz.userService.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.bitblizz.userService.dto.UserDto;
import com.bitblizz.userService.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers(){
        return userService.getUsers();
    }

    @GetMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUserByUsername(@PathVariable String username){
        return userService.getUserByUsername(username);
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    public boolean deleteUserByUsername(@PathVariable String username){
        return userService.delete(username);
    }
}
