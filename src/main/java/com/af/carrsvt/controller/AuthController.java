package com.af.carrsvt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.af.carrsvt.dto.AuthResponseDto;
import com.af.carrsvt.dto.LoginRequestDto;
import com.af.carrsvt.security.CustomerUserDetails;
import com.af.carrsvt.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@jakarta.validation.Valid @RequestBody LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomerUserDetails principal = (CustomerUserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(principal);

        return ResponseEntity.ok(
            new AuthResponseDto(
                "Bearer",
                accessToken,
                jwtService.getJwtExpirationSeconds(),
                principal.getUsername(),
                principal.getRole().name(),
                principal.getCustomerId()
            )
        );
    }
}
