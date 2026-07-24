package com.example.ecomerseapplication.Auth.helpers;

import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Others.GlobalConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class ClientTypeExtractor {
    public static Optional<ClientType> getClientType()
    {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes();

        return Optional.ofNullable((ClientType) attributes
                .getRequest()
                .getAttribute(GlobalConstants.CLIENT_TYPE_ATTRIBUTE));

    }
}
