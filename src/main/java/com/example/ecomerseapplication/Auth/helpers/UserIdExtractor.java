package com.example.ecomerseapplication.Auth.helpers;

import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.UserIdExtractException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class UserIdExtractor {

    public String getUserId() {
        try {
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            return jwt.getClaimAsString("sub");
        }
        catch (Exception e)
        {
            throw new UserIdExtractException("Error extracting user id: " + e.getMessage());
        }
    }
}
