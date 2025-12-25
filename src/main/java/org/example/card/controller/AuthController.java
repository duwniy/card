package org.example.card.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.example.card.security.JwtTokenProvider;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Test authentication endpoints")
@Log4j2
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/token/{userId}")
    @Operation(summary = "Generate test JWT token", description = "FOR TESTING ONLY - generates JWT token for given user ID")
    public String generateToken(@PathVariable Long userId) {
        log.info("Generating test token for user: {}", userId);
        return jwtTokenProvider.createToken(userId);
    }
}