package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta crear una orden con más unidades
 * de las disponibles en stock.
 *
 * El GlobalExceptionHandler la captura y retorna HTTP 409 Conflict,
 * indicando que la solicitud es válida pero entra en conflicto con
 * el estado actual del recurso.
 *
 * Uso en OrderService:
 *   if (product.getStock() < requestedQuantity) {
 *       throw new InsufficientStockException(product.getName(), product.getStock(), requestedQuantity);
 *   }
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    private final String productName;
    private final int availableStock;
    private final int requestedQuantity;

    /**
     * Constructor con contexto completo para mensajes de error informativos.
     *
     * @param productName       Nombre del producto con stock insuficiente.
     * @param availableStock    Stock disponible actualmente.
     * @param requestedQuantity Cantidad que el usuario intentó comprar.
     */
    public InsufficientStockException(String productName,
                                      int availableStock,
                                      int requestedQuantity) {
        super(String.format(
                "Stock insuficiente para '%s'. Disponible: %d, solicitado: %d.",
                productName, availableStock, requestedQuantity
        ));
        this.productName       = productName;
        this.availableStock    = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    // Getters explícitos (sin @Data en excepciones para evitar equals/hashCode problemático)

    public String getProductName()       { return productName; }
    public int    getAvailableStock()    { return availableStock; }
    public int    getRequestedQuantity() { return requestedQuantity; }
}
