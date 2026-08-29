package com.householdchores.backend.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public Map<String, String> test(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Map.of(
                "message", "Authentication works",
                "userId", jwt.getSubject()
        );
    }
}