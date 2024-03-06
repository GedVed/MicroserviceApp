package com.bitblizz.authenticationService.rabbit;


import com.bitblizz.authenticationService.model.User;
import com.bitblizz.authenticationService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class RabbitListen {

    private final UserRepository userRepository;

    @RabbitListener(queues = "auth_user.queue")
    public void deleteUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        user.ifPresent(userRepository::delete);
    }
}
