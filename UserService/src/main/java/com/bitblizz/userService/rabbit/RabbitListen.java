package com.bitblizz.userService.rabbit;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.bitblizz.userService.events.AuthEvent;
import com.bitblizz.userService.model.User;
import com.bitblizz.userService.repository.UserRepository;
import com.bitblizz.userService.events.UserEvent;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class RabbitListen {

    private final UserRepository userRepository;
    private final RabbitSender rabbitSender;



    @RabbitListener(queues = "auth.queue")
    public void createUser(AuthEvent event){

        UserPayload payload = event.getUserPayload();

        if(event.getEventType().equals("create")){
            User user = new User(UUID.randomUUID(), payload.getUsername(),
                    payload.getFirstname(), payload.getLastname());
            createUserEvent(user);
        }

        if(event.getEventType().equals("delete")){
            deleteUserEvent(payload.getUsername());
        }
    }


    private void createUserEvent(User user){
        User userSaved = userRepository.save(user);

        log.info("User sent");
        UserEvent CreatedUserEvent = new UserEvent(userSaved.getUsername(), "create");
        try {
            rabbitSender.sendMessage("user.exchange",
                    "user.create",
                    CreatedUserEvent);
        }catch (Exception exception){
            log.info(exception.getMessage());
        }
    }

    private void deleteUserEvent(String username){
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            return;
        }

        userRepository.delete(user.get());
        UserEvent deleteUserEvent = new UserEvent(username, "delete");
        try {
            rabbitSender.sendMessage("user.exchange",
                    "user.delete",
                    deleteUserEvent);
        }catch (Exception exception){
            log.info(exception.getMessage());
        }
    }
}
