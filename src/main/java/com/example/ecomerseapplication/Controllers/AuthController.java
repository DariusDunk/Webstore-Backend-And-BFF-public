package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.CustomerAccountRequest;
import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.Services.AuthService;
import com.example.ecomerseapplication.enums.UserRole;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("auth/")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("register")
    public ResponseEntity<?> registerUserKeycloak(@RequestBody @Valid CustomerAccountRequest customerAccountRequest) {//TODO ZAPISVANETO V BAZATA TRQBVA DA SE KRIPTIRA!!!

        authService.register(customerAccountRequest.firstName,
                customerAccountRequest.familyName,
                customerAccountRequest.password,
                customerAccountRequest.email,
                UserRole.CUSTOMER);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PostMapping("login")
    public ResponseEntity<?> loginUserKeycloak(@RequestBody @Valid UserLoginRequest request) {//todo trqbva refresh tokena v tablicata na sesiqta da se kriptira
        return ResponseEntity.ok(authService.login(request));

    }

    @GetMapping("logout")
    public ResponseEntity<?> logout() {
        try {

            return ResponseEntity.ok(authService.logout());

//            return ResponseEntity.ok(authService.createGuest(clientType));
        } catch (Exception e) {
            System.out.println("Error invalidating token or session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("tokens")
    public ResponseEntity<?> getTokensOfSession() {

        return ResponseEntity.ok(authService.refresh());
    }

    @GetMapping("session/guest/create/{clientType}")
    public ResponseEntity<?> createGuestSession(@PathVariable("clientType") String clientTypeName) {

      return ResponseEntity.status(HttpStatus.CREATED).body(authService.createGuest(clientTypeName));
    }

    @PostMapping("forgotten-password/{email}")
    public ResponseEntity<?> forgottenPasswordRequest(@PathVariable("email") String email) {

        System.out.println("inside forgotten password");

        authService.forgottenPasswordRequest(email);

        return ResponseEntity.ok().build();
    }


}
