package com.projectsaas.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    @MessageMapping("/notification.connect")
    @SendTo("/topic/public")
    public Map<String, Object> connect(@Payload Map<String, Object> message,
                                       SimpMessageHeaderAccessor headerAccessor) {

        String userId = (String) message.get("userId");
        headerAccessor.getSessionAttributes().put("userId", userId);

        log.info("User {} connected to WebSocket", userId);

        return Map.of(
                "type", "CONNECT",
                "content", "User " + userId + " connected",
                "userId", userId
        );
    }

    @MessageMapping("/notification.disconnect")
    @SendTo("/topic/public")
    public Map<String, Object> disconnect(@Payload Map<String, Object> message,
                                          SimpMessageHeaderAccessor headerAccessor) {

        String userId = (String) headerAccessor.getSessionAttributes().get("userId");

        log.info("User {} disconnected from WebSocket", userId);

        return Map.of(
                "type", "DISCONNECT",
                "content", "User " + userId + " disconnected",
                "userId", userId
        );
    }
}