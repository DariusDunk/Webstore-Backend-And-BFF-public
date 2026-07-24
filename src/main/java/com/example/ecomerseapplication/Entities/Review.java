package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "reviews", schema = "online_shop")
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private long id;

    @JoinColumn(name = "product_id")
    @ManyToOne
    private Product product;

    @JoinColumn(name = "customer_id_keyk")
    @ManyToOne
    private Customer customer;

    @Column(name = "review_text", columnDefinition = "character varying(500)")
    private String reviewText;

    private short rating;

    @Column(name = "post_timestamp")
    @CreationTimestamp
    private Instant postTimestamp;

    @Column(name = "verified_customer")
    private Boolean verifiedCustomer;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
