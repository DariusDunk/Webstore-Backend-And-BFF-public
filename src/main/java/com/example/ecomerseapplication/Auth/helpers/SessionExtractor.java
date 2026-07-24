package com.example.ecomerseapplication.Auth.helpers;

import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Others.GlobalConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class SessionExtractor {
    public static Optional<Session> getRequestSession() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes();

        return Optional.ofNullable((Session) attributes
                .getRequest()
                .getAttribute(GlobalConstants.SESSION_ATTRIBUTE));
    }
}
