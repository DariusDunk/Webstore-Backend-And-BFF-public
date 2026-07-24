package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "customers", schema = "online_shop")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"reviews","savedPurchaseDetails", "purchases"})
public class Customer {

    @Id
    @Column(name = "k_id")
    private String keycloakId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(columnDefinition = "character varying(50)")
    private String email;

    @Column(name = "phone_number", columnDefinition = "character varying(10)")
    private String phoneNumber;

    @CreationTimestamp
    @Column(name = "registration_date")
    private Instant registrationDate;

    @Column(name = "customer_pfp")
    private String customerPfp = "default_pfp.png";

    @OneToMany(mappedBy = "customer")
    private List<Purchase> purchases;

    @OneToOne(mappedBy = "customer")
    private SavedPurchaseDetails savedPurchaseDetails;

    @OneToMany(mappedBy = "favoriteOfCustomerId.customer", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<FavoriteOfCustomer> favoritesOfCustomer;

    @OneToMany(mappedBy = "customer")
    private List<Review> reviews;

    @OneToMany(mappedBy = "customer")
    private List<Session> sessions;

    public Customer(String keycloakId, String firstName, String lastName, String email) {
        this.keycloakId = keycloakId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public void updateCustomerData(String firstName, String lastName, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }
}
