package com.example.cinema.booking.domain.entities;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class BookingItem {
    private String id;
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;

    protected BookingItem() {}

    @Builder
    public BookingItem(String id, String productId, String productName, int quantity, BigDecimal unitPrice) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : java.util.UUID.randomUUID().toString();
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
