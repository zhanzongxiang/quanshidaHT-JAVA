package com.qsd.admin.payment;

import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.ErrorCode;
import com.qsd.admin.common.service.CryptoService;
import com.qsd.admin.config.WechatPayProperties;
import com.qsd.admin.payment.dto.PayMerchantConfigCreateRequest;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.mapper.PayMerchantConfigMapper;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.tenant.TenantContext;
import com.qsd.admin.tenant.TenantContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMerchantServiceTest {
    @Mock
    private PayMerchantConfigMapper payMerchantConfigMapper;

    @Mock
    private CryptoService cryptoService;

    private PaymentMerchantService paymentMerchantService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new TenantContext(1L, "default", "Default Tenant"));
        paymentMerchantService = new PaymentMerchantService(
            payMerchantConfigMapper,
            new WechatPayProperties(
                true,
                false,
                "certs",
                6,
                "Default Merchant",
                "wx-demo",
                "",
                "demo-mch",
                "http://localhost:8080/api/payment/callback/wechat",
                "",
                "",
                "",
                ""
            ),
            cryptoService
        );
    }

    @Test
    void shouldEncryptSensitiveFieldsWhenCreatingMerchantConfig() {
        when(payMerchantConfigMapper.selectAllActiveRows(1L)).thenReturn(java.util.List.of(new PayMerchantConfig()));
        when(cryptoService.isEncrypted("app-secret-123")).thenReturn(false);
        when(cryptoService.isEncrypted("12345678901234567890123456789012")).thenReturn(false);
        when(cryptoService.encrypt("app-secret-123")).thenReturn("enc-app-secret");
        when(cryptoService.encrypt("12345678901234567890123456789012")).thenReturn("enc-api-v3");
        doAnswer(invocation -> {
            PayMerchantConfig config = invocation.getArgument(0);
            config.setId(8L);
            return 1;
        }).when(payMerchantConfigMapper).insert(any(PayMerchantConfig.class));

        paymentMerchantService.createMerchantConfig(new PayMerchantConfigCreateRequest(
            "Acme Merchant",
            "acme_merchant",
            "mch-001",
            "wx-app-001",
            "app-secret-123",
            "https://pay.example.com/api/payment/callback/wechat",
            "12345678901234567890123456789012",
            "E:/secure/apiclient_key.pem",
            "serial-001",
            "E:/secure/platform.pem",
            true,
            "ready"
        ));

        ArgumentCaptor<PayMerchantConfig> captor = ArgumentCaptor.forClass(PayMerchantConfig.class);
        verify(payMerchantConfigMapper).insert(captor.capture());
        assertEquals("enc-app-secret", captor.getValue().getAppSecret());
        assertEquals("enc-api-v3", captor.getValue().getApiV3Key());
    }

    @Test
    void shouldRejectInvalidApiV3KeyLength() {
        when(payMerchantConfigMapper.selectAllActiveRows(1L)).thenReturn(java.util.List.of(new PayMerchantConfig()));

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> paymentMerchantService.createMerchantConfig(new PayMerchantConfigCreateRequest(
                "Acme Merchant",
                "acme_merchant",
                "mch-001",
                "wx-app-001",
                "",
                "https://pay.example.com/api/payment/callback/wechat",
                "short-key",
                "",
                "",
                "",
                true,
                ""
            ))
        );

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getCode());
        assertEquals("apiV3Key must be exactly 32 characters when provided", ex.getMessage());
        verify(payMerchantConfigMapper, never()).insert(any(PayMerchantConfig.class));
    }

    @Test
    void shouldRejectIncompleteCertificateBundle() {
        when(payMerchantConfigMapper.selectAllActiveRows(1L)).thenReturn(java.util.List.of(new PayMerchantConfig()));

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> paymentMerchantService.createMerchantConfig(new PayMerchantConfigCreateRequest(
                "Acme Merchant",
                "acme_merchant",
                "mch-001",
                "wx-app-001",
                "",
                "https://pay.example.com/api/payment/callback/wechat",
                "",
                "E:/secure/apiclient_key.pem",
                "",
                "",
                true,
                ""
            ))
        );

        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getCode());
        assertEquals(
            "privateKeyPath, merchantSerialNo, and platformCertificatePath must be provided together",
            ex.getMessage()
        );
        verify(payMerchantConfigMapper, never()).insert(any(PayMerchantConfig.class));
    }
}
