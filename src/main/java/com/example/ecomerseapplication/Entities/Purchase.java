package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.BadPurchaseCancelRequestException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.BadPurchaseRefundRequestException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidPurchaseActionException;
import com.example.ecomerseapplication.enums.DeliveryStatus;
import com.example.ecomerseapplication.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Entity
@Table(name = "purchases", schema = "online_shop")
@Data
@NoArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private long id;

    @JoinColumn(name = "customer_id")
    @ManyToOne
    private Customer customer;

    @Column(name = "purchase_date", updatable = false)
    @CreationTimestamp
    private Instant date;

    @Column(name = "total_cost")
    private int totalCost;

    @Column(name = "contact_name", columnDefinition = "character varying(50)")
    private String contactName;

    @Column(name = "contact_number", columnDefinition = "character varying(10)")
    private String contactNumber;

    private String address;

    private String purchaseCode;

    @JoinColumn(name = "session_id")
    @ManyToOne
    private Session session;

    @Column(name = "shipping_fee")
    private int shippingFee;

    @Column(name = "product_total")
    private int productTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DeliveryStatus deliveryStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentMethod paymentMethod;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "purchaseCartId.purchase",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH},
            orphanRemoval = true)
    List<PurchaseCart> purchaseProducts;

    @Column(name = "delivery_date")
    private Instant deliveryDate;

    public Purchase(Customer customer,
                    int totalCost,
                    String contactName,
                    String contactNumber,
                    String address,
                    String purchaseCode,
                    int shippingFee,
                    int productTotal,
                    PaymentMethod paymentMethod) {

        this.customer = customer;
        this.totalCost = totalCost;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.address = address;
        this.purchaseCode = purchaseCode;
        this.shippingFee = shippingFee;
        this.productTotal = productTotal;
        this.deliveryStatus = DeliveryStatus.PROCESSING;
        this.paymentMethod = paymentMethod;
    }

    public Purchase(Session session,
                    int totalCost,
                    String contactName,
                    String contactNumber,
                    String address,
                    String purchaseCode,
                    int shippingFee,
                    int productTotal,
                    PaymentMethod paymentMethod,
                    String email) {

        this.session = session;
        this.totalCost = totalCost;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.address = address;
        this.purchaseCode = purchaseCode;
        this.shippingFee = shippingFee;
        this.productTotal = productTotal;
        this.deliveryStatus = DeliveryStatus.PROCESSING;
        this.paymentMethod = paymentMethod;
        this.email = email;
    }

    public void cancelPurchase() {

        if (!this.getDeliveryStatus().equals(DeliveryStatus.PROCESSING)) {
            throw new BadPurchaseCancelRequestException("Purchase is not in processing status");
        }

        this.deliveryStatus = DeliveryStatus.CANCELLED;
    }

    public void setRefundRequested(long refundDays) {
        if (!this.getDeliveryStatus().equals(DeliveryStatus.DELIVERED)) {
            throw new BadPurchaseRefundRequestException("Purchase already has refund request or has been refunded",
                    "Невалидна заявка",
                    "Покупката не може да се върне ако още не е доставена или, вече е върната/отказана");
        }

        Instant deliveryDate = this.getDeliveryDate();
        Instant now = Instant.now();
        Instant maxRefundDate = deliveryDate.plus(refundDays, ChronoUnit.DAYS);
        if (now.isAfter(maxRefundDate)) {
            throw new BadPurchaseRefundRequestException("Purchase refund time passed",
                    "Връщане отказано",
                    "Периодът за връщане на поръчката вече е изтекъл");
        }

        this.setDeliveryStatus(DeliveryStatus.REFUND_REQUESTED);
    }

    public void deliverPurchase() {
        if (this.deliveryStatus.equals(DeliveryStatus.SHIPPED)) {
            this.setDeliveryStatus(DeliveryStatus.DELIVERED);
            this.setDeliveryDate(Instant.now());
        } else
            throw new InvalidPurchaseActionException("Cannot perform action: DELIVER on purchase with status: " + this.deliveryStatus.name());
    }

    public void shipPurchase() {
        if (this.deliveryStatus.equals(DeliveryStatus.PROCESSING))
            this.setDeliveryStatus(DeliveryStatus.SHIPPED);
        else
            throw new InvalidPurchaseActionException("Cannot perform action: SHIP on purchase with status: " + this.deliveryStatus.name());
    }

    public void approveRefund() {
        if (this.deliveryStatus.equals(DeliveryStatus.REFUND_REQUESTED))
            this.setDeliveryStatus(DeliveryStatus.REFUNDED);
        else
            throw new InvalidPurchaseActionException("Cannot perform action: APPROVE REFUND on purchase with status: " + this.deliveryStatus.name());
    }

    public void rejectRefund() {
        if (this.deliveryStatus.equals(DeliveryStatus.REFUND_REQUESTED))
            this.setDeliveryStatus(DeliveryStatus.DELIVERED);
        else
            throw new InvalidPurchaseActionException("Cannot perform action: REJECT REFUND on purchase with status: " + this.deliveryStatus.name());

    }
}
