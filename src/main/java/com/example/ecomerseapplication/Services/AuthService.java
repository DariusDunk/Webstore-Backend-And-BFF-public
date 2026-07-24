package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Auth.helpers.SessionExtractor;
import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.Cart;
import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidSessionException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.LoginFailedException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.RegistrationFailedException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.UserAlreadyExistsException;
import com.example.ecomerseapplication.Mappers.LoginResponseMapper;
import com.example.ecomerseapplication.Mappers.RefreshResponseMapper;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.enums.UserRole;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private final KeycloakService keycloakService;
    private final CustomerService customerService;
    private final SessionService sessionService;
    private final ClientTypeService clientTypeService;
    private final CartService cartService;
    private final CartProductService cartProductService;

    @Autowired
    public AuthService(KeycloakService keycloakService, CustomerService customerService, SessionService sessionService, ClientTypeService clientTypeService, CartService cartService, CartProductService cartProductService) {
        this.keycloakService = keycloakService;
        this.customerService = customerService;
        this.sessionService = sessionService;
        this.clientTypeService = clientTypeService;
        this.cartService = cartService;
        this.cartProductService = cartProductService;
    }

    @Transactional
    public void register(String firstname, String lastName, String password, String email, UserRole userRole) {
        String userId = null;
        try {
            userId = keycloakService.registerUser(firstname, lastName, password, email, userRole);

            if (userId != null) {
                Customer customer = new Customer(userId, firstname, lastName, email);
                customer = customerService.save(customer);

                cartService.save(new Cart(customer));
            }

        } catch (Exception e) {

            System.out.println("Error registering user: " + e.getMessage());
            if (userId != null) {
                try {
                    System.out.println("Rolling back user creation");
                    keycloakService.deleteUser(userId);
                } catch (Exception ex) {
                    System.out.println("Error rollback deleting user: " + ex.getMessage());
                }
            }

            if (e instanceof ValidationException
                    || e instanceof UserAlreadyExistsException
                    || e instanceof RegistrationFailedException) {
                throw e;

            }
            throw new RegistrationFailedException("Registration failed");
        }
    }

    private String extractIdFromToken(String token) throws ParseException {
        SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
        return signedJWT.getJWTClaimsSet().getSubject();
    }

    @Transactional
    public LoginResponse login(UserLoginRequest request) {
        KeycloakTokenResponse tokenResponse = null;
        try {
            tokenResponse = keycloakService.loginUser(request);
            String userId = extractIdFromToken(tokenResponse.accessToken());
            Customer customer = customerService.getById(userId);
//            ClientType clientType = clientTypeService.getByTypeName(request.clientType());
            Session session = sessionService.getRequestSession();
//            String sessionId = SessionExtractor.getRequestSession();

//            if (sessionId != null) {
//                session = sessionService.getOrCreateById(sessionId, clientType);
                session = sessionService.guestToLoginSession(session, tokenResponse, request.rememberMe(), customer);
                if (cartService.existsBySession(session)) {
                    cartProductService.mergeCarts(session, customer);
                }
//            } else {
//                session = sessionService.createAuthenticatedSession(tokenResponse.refreshToken(), customer, clientType, request.rememberMe(), tokenResponse.refreshExpiresIn());
//            }

            return LoginResponseMapper.fromKeycloakResponseAndSession(tokenResponse, session);
        } catch (Exception e) {
            if (tokenResponse != null) {
                keycloakService.invalidateRefreshToken(tokenResponse.refreshToken());
                throw new LoginFailedException("Login failed, rolling back session creation: " + e.getMessage());
            }
            throw new LoginFailedException("Login failed: " + e.getMessage());
        }
    }

    @Transactional
    public GuestSessionResponse logout() {

        Session session = sessionService.getRequestSession();

        try {

            String refreshToken = session.getRefreshToken();
            session = sessionService.AuthToGuestSession(session);

            keycloakService.invalidateRefreshToken(refreshToken);
        } catch (Exception e) {
            System.out.println("-------------------------------------------------------");
            System.out.println("Error invalidating refresh token in keycloak: " + e.getMessage());
            System.out.println("-------------------------------------------------------");
        }

        return new GuestSessionResponse(session.getSessionId(),
                Duration.between(Instant.now(), session.getExpiresAt()).getSeconds());
    }

    @Transactional
    public RefreshResponse refresh() {

        String refreshToken;
        TokenRefreshResponse tokenRefreshResponse;
        Session session = null;
        try {
            session = SessionExtractor.getRequestSession().orElse(null);

            if (session == null)
                throw new InvalidSessionException("Session not found/null in refresh");

//            System.out.println("\n" +
//                    "----------------------------------\nSession in refresh is: " + session.getSessionId()
//                    + "\nIs expired or revked: "
//                    + (session.isExpired() || session.getIsRevoked()) + "\n" +
//                    "----------------------------------\n");

            if (session.getIsRevoked() || session.isExpired()) {
                session = sessionService.createGuestSession(session.getClientType());
                sessionService.save(session);
                return new RefreshResponse(null,
                        SessionService.calculateSessionTTLSeconds(session.getExpiresAt()),
                        true,
                        false,
                        session.getSessionId());
            }
            else
                refreshToken = session.getRefreshToken();


            tokenRefreshResponse = keycloakService.refreshBothTokens(refreshToken);

//            System.out.println("[SUCCESS] New refresh token in keycloak: " + tokenRefreshResponse.refreshToken() );

        } catch (Exception e) {
            System.out.println("Error refreshing token in keycloak, session will be treated as a guest: \n" + e.getMessage() + "\n -------------------------------------------------------");


            if (session == null) {

                ClientType clientType = clientTypeService.getFromRequest();
                session = sessionService.createGuestSession(clientType);
            }
            else
            {
                if (cartService.existsBySession(session))
                    session.markAsGuestWithCart(GlobalConstants.CART_GUEST_SESSION_TTL_DAYS);
                else
                    session.markAsGuestWithoutCart(GlobalConstants.LOW_PRIORITY_GUEST_SESSION_TTL_MINUTES);
            }

            sessionService.save(session);

            return new RefreshResponse(null,
                    Duration.between(Instant.now(), session.getExpiresAt()).getSeconds(),
                    true,
                    false,
                    session.getSessionId());
        }

        try {

            session.markAsAuthenticated(session.getCustomer(),
                    tokenRefreshResponse.refreshToken(),
                    session.getIsRememberMeSession(),
                    tokenRefreshResponse.refreshExpiresIn(),
                    GlobalConstants.NORMAL_SESSION_TTL_HOURS);

            sessionService.save(session);

            return RefreshResponseMapper.tokenRefreshToRefreshResponse(tokenRefreshResponse,
                    session.getExpiresAt(),
                    false,
                    session.getIsRememberMeSession(),
                    session.getSessionId());
        } catch (Exception e) {
            throw new InvalidSessionException("Session persist failed: " + e.getMessage());
        }
    }

    public GuestSessionResponse createGuest(String clientTypeName) {

        ClientType clientType = clientTypeService.getByTypeName(clientTypeName);

        Session session = sessionService.createGuestSession(clientType);

        return new GuestSessionResponse(session.getSessionId(),
                Duration.between(Instant.now(), session.getExpiresAt()).getSeconds());
    }

    public void forgottenPasswordRequest(String email) {

        if (!email.contains("@")) {
            throw new ValidationException("Въведеният имейл е невалиден");
        }

        Customer customer = customerService.getByEmail(email);

        keycloakService.passwordChangeRequestForCustomerId(customer.getKeycloakId());
    }
}
