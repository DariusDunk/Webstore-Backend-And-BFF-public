package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts", schema = "online_shop")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cart {

    @Column(name = "cart_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne()
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne()
    @JoinColumn(name = "session_id")
    private Session session;

    public Cart(Customer customer) {
        this.customer = customer;
    }
    public Cart(Session session) {
        this.session = session;
    }
}
