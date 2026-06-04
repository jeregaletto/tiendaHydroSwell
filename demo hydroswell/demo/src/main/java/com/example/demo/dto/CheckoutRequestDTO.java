package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * DTO para recibir el carrito de compras desde Next.js
 * al momento de iniciar el pago.
 */
@Getter
@Setter
public class CheckoutRequestDTO {
    
    private String shippingAddress;
    private List<CartItemDTO> items;

    @Getter
    @Setter
    public static class CartItemDTO {
        private Long productId;
        private Integer quantity;
    }
}