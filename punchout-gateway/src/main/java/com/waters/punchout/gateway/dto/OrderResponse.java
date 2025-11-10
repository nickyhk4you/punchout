package com.waters.punchout.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String muleOrderId;
    private String status;
    private String message;
    
    public OrderResponse(String orderId, String muleOrderId, String status) {
        this.orderId = orderId;
        this.muleOrderId = muleOrderId;
        this.status = status;
    }
}
