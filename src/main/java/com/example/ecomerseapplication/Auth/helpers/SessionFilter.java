package com.example.ecomerseapplication.Auth.helpers;

import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Mappers.SessionMapper;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.Services.ClientTypeService;
import com.example.ecomerseapplication.Services.SessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.time.Instant;

@Component
public class SessionFilter extends OncePerRequestFilter {

    private final SessionService sessionService;
    private final ClientTypeService clientTypeService;
    private final EndpointMatcher endpointMatcher;
    private final ObjectMapper mapper = new ObjectMapper();

    public SessionFilter(SessionService sessionService, ClientTypeService clientTypeService, EndpointMatcher endpointMatcher) {
        this.sessionService = sessionService;
        this.clientTypeService = clientTypeService;
        this.endpointMatcher = endpointMatcher;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        String requestLabel = "[For request: " + request.getRequestURI() + "]";

        System.out.println(requestLabel);

        String accessToken = request.getHeader("Authorization");
        Session session = getRequestSession(request);

        if (isInvalidAuthSession(session, accessToken)) {
            wrappedResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired auth session");
            wrappedResponse.copyBodyToResponse();
            return;
        }

        if (isPublicEndpoint(request)) {

            /*----------------------PUBLIC ENDPOINT-----------------------------*/
            filterChain.doFilter(request, wrappedResponse);
            /*----------------------PUBLIC ENDPOINT-----------------------------*/
            wrappedResponse.copyBodyToResponse();
            return;
        }

        if (isRefreshEndpoint(request)) {

            ClientType clientType = getSessionClientType(request);
            setUpSessionAndClientRequestHeaders(request, session, clientType);
            /*----------------------REFRESH ENDPOINT-----------------------------*/
            filterChain.doFilter(request, wrappedResponse);
            /*----------------------REFRESH ENDPOINT-----------------------------*/
            wrappedResponse.copyBodyToResponse();
            return;
        }

        session = replaceIfInvalidSession(session, request);
        Instant initialSessionExpiry = session.getExpiresAt();
        boolean isReplaced = Boolean.TRUE.equals(request.getAttribute(GlobalConstants.IS_REPLACED_ATTRIBUTE));
        /*----------------------ENDPOINT-----------------------------*/
        filterChain.doFilter(request, wrappedResponse);
        /*----------------------ENDPOINT-----------------------------*/
        sessionService.updateActivity(session);

        Instant finalSessionExpiry = session.getExpiresAt();

        if (isReplaced || !initialSessionExpiry.equals(finalSessionExpiry)) {
//
//            System.out.println(" \n" +
//                    "----------------------------------\nThe session has been replaced or the expiry time has changed.\n" +
//                    "Old expiry: "+ initialSessionExpiry + " new expiri: "+ finalSessionExpiry + " \n" +
//                    "----------------------------------\n");

            try {
                String json = mapper.writeValueAsString(
                        SessionMapper.entToHeaderResponse(session, isReplaced)
                );
                wrappedResponse.setHeader("x-session-info", json);
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize session header", e);
            }
        }
        wrappedResponse.copyBodyToResponse();
//        Collection<String> headerNames = wrappedResponse.getHeaderNames();

//        System.out.println(requestLabel + "------ Response Headers ------");
//
//        for (String headerName : headerNames) {
//            System.out.println(headerName + ": " + response.getHeader(headerName));
//        }
//
//        System.out.println("------------------------------");

    }

    private boolean isInvalidAuthSession(Session session, String accessToken) {

        if (accessToken != null && !accessToken.isBlank()
                && (session == null || (session.getIsRevoked() || session.isExpired()))
        ) {
            return true;
        }

        if (session != null) {

            Customer sessionCustomer = session.getCustomer();

            return sessionCustomer != null
                    && (session.getIsRevoked() || session.isExpired());
        }
        return false;
    }

    private Session getRequestSession(HttpServletRequest request) {
//        String path = request.getRequestURI();
        String sessionId = request.getHeader(GlobalConstants.SESSION_ID_HEADER);

//        System.out.println("\n" +
//                "----------------------------------\n" +
//                "FOR REQUEST ["+path+"]RECEIVED SESSION ID TO FILTER: " + sessionId + "\n" +
//                "----------------------------------\n");

        if (sessionId != null) {
            return sessionService.getByIdOptional(sessionId).orElse(null);
        }
        return null;
    }

    private ClientType getSessionClientType(HttpServletRequest request) {
        String clientTypeName = request.getHeader(GlobalConstants.CLIENT_TYPE_HEADER);
//        String path = request.getRequestURI();

//        System.out.println("FOR REQUEST ["+path+"]CLIENT TYPE: " + clientTypeName + "\n-----------------------------------------\n");

        if (clientTypeName == null)
            clientTypeName = "Web";

        return clientTypeService.getByTypeName(clientTypeName);
    }

    private void setUpSessionAndClientRequestHeaders(HttpServletRequest request, Session session, ClientType clientType) {
        request.setAttribute(GlobalConstants.CLIENT_TYPE_ATTRIBUTE, clientType);
        request.setAttribute(GlobalConstants.SESSION_ATTRIBUTE, session);
    }

    private Session replaceIfInvalidSession(Session session, @NonNull HttpServletRequest request) {

        ClientType clientType = getSessionClientType(request);

        if (session == null || session.getIsRevoked() || session.getExpiresAt().isBefore(Instant.now())) {

            session = sessionService.createGuestSession(clientType);
            request.setAttribute(GlobalConstants.IS_REPLACED_ATTRIBUTE, true);

//            System.out.println("\n" +
//                    "----------------------------------\n" +
//                    "SESSION REPLACED WITH NEW SESSION: "+ session.getSessionId() +
//                    "\n----------------------------------\n");

        } else {
            request.setAttribute(GlobalConstants.IS_REPLACED_ATTRIBUTE, false);
        }

        setUpSessionAndClientRequestHeaders(request, session, clientType);

        return session;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();

//        System.out.println("\n" +
//                "----------------------------------\nCurrent Request Path: '" + path + "'\n" +
//                "----------------------------------\n");

        //        System.out.println("Endpoint " + path + " is " + (isPublic ? "public" : "private") + "\n");
        return endpointMatcher.isPublic(path);
    }

    private boolean isRefreshEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();

        return endpointMatcher.isRefreshToken(path);
    }

}
