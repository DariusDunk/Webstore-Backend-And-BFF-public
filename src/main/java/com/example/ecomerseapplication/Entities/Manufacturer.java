package com.example.ecomerseapplication.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "manufacturers", schema = "online_shop")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "products")
@AllArgsConstructor
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manufacturer_id")
    private int id;

    @Column(name = "manufacturer_name", columnDefinition = "character varying(30)")
    private String manufacturerName;

    @JsonIgnore
    @OneToMany(mappedBy = "manufacturer", fetch = FetchType.LAZY)
    private List<Product> products;
}
