package com.bitblizz.userService.service;

import com.bitblizz.userService.dto.UserDto;
import com.bitblizz.userService.model.User;
import com.bitblizz.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.bitblizz.userService.events.UserEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;


    public List<UserDto> getUsers(){
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> new UserDto(user.getUsername(),user.getFirstname(), user.getLastname())).collect(Collectors.toList());
    }

    public UserDto getUserByUsername(String username){
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(X -> new UserDto(X.getUsername(),X.getFirstname(),X.getLastname())).orElse(null);
    }

    public boolean delete(String username){
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            return false;
        }
        userRepository.delete(user.get());
        UserEvent userEvent = new UserEvent(user.get().getUserID().toString(),"delete");
        rabbitTemplate.convertAndSend("user.exchange", "user.delete", userEvent);
        return true;
    }
}
