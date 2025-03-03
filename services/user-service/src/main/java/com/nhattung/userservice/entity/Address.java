package com.nhattung.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;
    private String district;
    private String city;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserProfile user;

}
