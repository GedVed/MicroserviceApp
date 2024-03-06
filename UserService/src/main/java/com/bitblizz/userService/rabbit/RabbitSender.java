package com.bitblizz.userService.rabbit;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class RabbitSender {

    private final AmqpTemplate amqpTemplate;

    public void sendMessage(String exchange, String key, Object messege){
        amqpTemplate.convertAndSend(exchange, key, messege);
    }
}
