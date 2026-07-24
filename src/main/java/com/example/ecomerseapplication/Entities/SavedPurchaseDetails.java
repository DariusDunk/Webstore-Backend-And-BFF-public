package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.DTOs.requests.SavedRecipientDetailsRequest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "saved_purchase_details", schema = "online_shop")
@Data
@NoArgsConstructor
public class SavedPurchaseDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private long id;

    @Column(name = "contact_name", columnDefinition = "character varying(50)")
    private String contactName;

    @Column(name = "contact_number", columnDefinition = "character varying(10)")
    private String contactNumber;

    private String address;

    @JoinColumn(name = "customer_keyk_id", referencedColumnName = "k_id")
    @OneToOne
    private Customer customer;

    public SavedPurchaseDetails(SavedRecipientDetailsRequest savedPurchaseDetailsResponse, Customer customer) {
        this.contactName = savedPurchaseDetailsResponse.contactName;
        this.contactNumber = savedPurchaseDetailsResponse.contactNumber;
        this.address = savedPurchaseDetailsResponse.address;
        this.customer = customer;
    }
}
