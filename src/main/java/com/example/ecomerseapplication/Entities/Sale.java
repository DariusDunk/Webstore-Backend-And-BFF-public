package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "sales", schema = "online_shop")
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Sale {

    @Column(name = "sale_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "discount_percent")
    private Short discountPercent;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "is_active")
    private Boolean isActive;

    public void updateSale(String name, Short discountPercent, Instant startDate, Instant endDate, Boolean isActive){
        this.name = name;
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public Boolean isExpired(){
        return Instant.now().isAfter(endDate);
    }

}
