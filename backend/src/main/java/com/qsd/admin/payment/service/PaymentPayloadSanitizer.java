package com.qsd.admin.payment.service;

import java.util.regex.Pattern;

public final class PaymentPayloadSanitizer {
    private static final String REDACTED = "***";
    private static final Pattern[] JSON_SECRET_PATTERNS = new Pattern[] {
        Pattern.compile("(?i)(\"appSecret\"\\s*:\\s*\")([^\"]*)(\")"),
        Pattern.compile("(?i)(\"apiV3Key\"\\s*:\\s*\")([^\"]*)(\")"),
        Pattern.compile("(?i)(\"privateKey\"\\s*:\\s*\")([^\"]*)(\")"),
        Pattern.compile("(?i)(\"privateKeyPath\"\\s*:\\s*\")([^\"]*)(\")"),
        Pattern.compile("(?i)(\"platformCertificatePath\"\\s*:\\s*\")([^\"]*)(\")"),
        Pattern.compile("(?i)(\"authorization\"\\s*:\\s*\")([^\"]*)(\")"),
        Pattern.compile("(?i)(\"paySign\"\\s*:\\s*\")([^\"]*)(\")"),
        Pattern.compile("(?i)(\"serialNo\"\\s*:\\s*\")([^\"]*)(\")")
    };
    private static final Pattern[] TEXT_SECRET_PATTERNS = new Pattern[] {
        Pattern.compile("(?i)(appSecret=)([^&\\s]+)"),
        Pattern.compile("(?i)(apiV3Key=)([^&\\s]+)"),
        Pattern.compile("(?i)(privateKey=)([^&\\s]+)"),
        Pattern.compile("(?i)(privateKeyPath=)([^&\\s]+)"),
        Pattern.compile("(?i)(platformCertificatePath=)([^&\\s]+)"),
        Pattern.compile("(?i)(Authorization:\\s*)([^\\r\\n]+)")
    };

    private PaymentPayloadSanitizer() {
    }

    public static String sanitizeForTransactionLog(String payload) {
        if (payload == null || payload.isBlank()) {
            return "";
        }

        String sanitized = payload.trim();
        for (Pattern pattern : JSON_SECRET_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("$1" + REDACTED + "$3");
        }
        for (Pattern pattern : TEXT_SECRET_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("$1" + REDACTED);
        }
        return sanitized;
    }
}
