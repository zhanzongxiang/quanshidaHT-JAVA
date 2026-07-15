package com.qsd.admin.payment.dto;

public record PaymentTransactionResponse(
    Long id,
    String transactionType,
    String transactionStatus,
    String externalTransactionNo,
    String externalOutTradeNo,
    String successTime,
    String createdAt
) {
}
