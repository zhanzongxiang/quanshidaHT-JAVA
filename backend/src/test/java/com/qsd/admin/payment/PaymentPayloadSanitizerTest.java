package com.qsd.admin.payment;

import com.qsd.admin.payment.service.PaymentPayloadSanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentPayloadSanitizerTest {

    @Test
    void shouldRedactSensitiveJsonFields() {
        String sanitized = PaymentPayloadSanitizer.sanitizeForTransactionLog(
            """
                {"appSecret":"secret-1","apiV3Key":"secret-2","paySign":"secret-3","safe":"ok"}
                """
        );

        assertFalse(sanitized.contains("secret-1"));
        assertFalse(sanitized.contains("secret-2"));
        assertFalse(sanitized.contains("secret-3"));
        assertTrue(sanitized.contains("\"safe\":\"ok\""));
    }

    @Test
    void shouldRedactSensitivePlainTextTokens() {
        String sanitized = PaymentPayloadSanitizer.sanitizeForTransactionLog(
            "Authorization: Bearer abcdef appSecret=secret-value apiV3Key=another-secret"
        );

        assertFalse(sanitized.contains("abcdef"));
        assertFalse(sanitized.contains("secret-value"));
        assertFalse(sanitized.contains("another-secret"));
        assertTrue(sanitized.contains("Authorization: ***"));
    }
}
