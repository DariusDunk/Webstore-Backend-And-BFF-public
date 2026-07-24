package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.CompositeIdClasses.FavoriteOfCustomerId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(schema = "online_shop",name = "favourites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteOfCustomer {

    @EmbeddedId
    private FavoriteOfCustomerId favoriteOfCustomerId;

    @Column(name = "date_added")
    private Instant dateAdded;
}
