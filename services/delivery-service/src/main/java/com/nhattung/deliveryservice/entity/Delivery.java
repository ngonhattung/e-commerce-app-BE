package com.nhattung.deliveryservice.entity;

import com.nhattung.deliveryservice.enums.DeliveryMethod;
import com.nhattung.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shippingAddress;
    @Enumerated(EnumType.STRING)
    private DeliveryMethod shippingMethod;

    @Enumerated(EnumType.STRING)
    private OrderStatus shippingStatus;

    private LocalDate shippingDate;
    private String orderId;

    @CreationTimestamp
    private Timestamp createdAt;
    @UpdateTimestamp
    private Timestamp updatedAt;
}
