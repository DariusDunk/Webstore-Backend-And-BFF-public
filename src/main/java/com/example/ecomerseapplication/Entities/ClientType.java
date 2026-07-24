package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity()
@Table(name = "client_types", schema = "online_shop")
@NoArgsConstructor
@Data
public class ClientType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_type_id")
    private Long clientTypeId;

    @Column(name = "type_name")
    private String clientTypeName;
}
