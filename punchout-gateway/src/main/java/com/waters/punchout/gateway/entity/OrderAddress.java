package com.waters.punchout.gateway.entity;

import lombok.Data;

@Data
public class OrderAddress {
    private String addressId;
    private String name;
    private String deliverTo;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String email;
    private String phone;
}
