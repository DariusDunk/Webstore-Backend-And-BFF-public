package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "sessions", schema = "online_shop")
@Data
@NoArgsConstructor
public class Session {

    @Id
    private String sessionId;

    @ManyToOne()
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne()
    @JoinColumn(name = "client_type")
    private ClientType clientType;

    @Column(name = "is_guest")
    private Boolean isGuest;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @CreationTimestamp
    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "is_revoked")
    private Boolean isRevoked;

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    @Column(name = "is_remember_me_session")
    private Boolean isRememberMeSession;

    @Column(name = "revoked_at")
    private Instant revokedAt;


    public void markAsGuestWithoutCart(int lowPriorityMinutes) {
        MarkAsGuest();
        this.expiresAt = Instant.now().plus(lowPriorityMinutes, ChronoUnit.MINUTES);

    }

    private void MarkAsGuest() {
        this.isGuest = true;
        this.customer = null;
        this.refreshToken = null;
        this.isRememberMeSession = false;
        this.isRevoked = false;
        this.lastActivityAt = Instant.now();
    }

    public void markAsGuestWithCart(int cartTtlDays) {
        MarkAsGuest();
        this.expiresAt = Instant.now().plus(cartTtlDays, ChronoUnit.DAYS);

    }

    public void markAsCartActive(int cartTtlDays) {

        if (Boolean.TRUE.equals(this.isGuest)) {
            this.expiresAt = Instant.now().plus(cartTtlDays, ChronoUnit.DAYS);
        }
    }

    public void markAsAuthenticated(Customer customer, String refreshToken, boolean isRememberMe, long refreshExpiresInSeconds, int normalTtlHours) {
        this.isGuest = false;
        this.customer = customer;
        this.refreshToken = refreshToken;
        this.isRememberMeSession = isRememberMe;
        this.isRevoked = false;
        this.lastActivityAt = Instant.now();

        if (isRememberMe) {
            this.expiresAt = Instant.now().plus(refreshExpiresInSeconds, ChronoUnit.SECONDS);
        } else {
            this.expiresAt = Instant.now().plus(normalTtlHours, ChronoUnit.HOURS);
        }
    }

    public boolean registerActivity(boolean hasCart, int lowPriorityMinutes, int cartTtlDays) {
        Instant now = Instant.now();

        if (this.lastActivityAt != null
                && now.isAfter(this.lastActivityAt.plus(5, ChronoUnit.MINUTES))
                && !Boolean.TRUE.equals(this.isRevoked)
        ) {
            this.lastActivityAt = now;

            if (Boolean.TRUE.equals(this.isGuest)) {
                if (hasCart) {
                    this.expiresAt = Instant.now().plus(cartTtlDays, ChronoUnit.DAYS);
                } else {
                    this.expiresAt = Instant.now().plus(lowPriorityMinutes, ChronoUnit.MINUTES);
                }
            }
            return true;
        }
        return false;
    }

    public void revoke() {
        this.isRevoked = true;
        this.revokedAt = Instant.now();
        this.refreshToken = null;
    }




    @Override
    public String toString() {
        return "Session{" +
                "refreshToken='" + refreshToken + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", customer=" + ((customer!=null)? customer.getFirstName() + " " + customer.getLastName():" " )+
                ", clientType=" + (clientType!=null? clientType :" ") +
                ", isGuest=" + isGuest +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", isRevoked=" + isRevoked +
                ", isRememberMeSession=" + isRememberMeSession +
                '}';
    }

    //todo v byde6te i za location 6te ima ne6to
}
