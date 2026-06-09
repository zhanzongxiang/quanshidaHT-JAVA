package com.qsd.admin.payment.service;

import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayOrder;
import com.qsd.admin.payment.mapper.PayOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentScheduler {
    private static final Logger log = LoggerFactory.getLogger(PaymentScheduler.class);

    private final PayOrderMapper payOrderMapper;
    private final WechatPayGateway wechatPayGateway;
    private final PaymentMerchantService paymentMerchantService;

    public PaymentScheduler(
        PayOrderMapper payOrderMapper,
        WechatPayGateway wechatPayGateway,
        PaymentMerchantService paymentMerchantService
    ) {
        this.payOrderMapper = payOrderMapper;
        this.wechatPayGateway = wechatPayGateway;
        this.paymentMerchantService = paymentMerchantService;
    }

    @Scheduled(fixedDelay = 300000)
    public void closeExpiredOrders() {
        List<PayOrder> expired = payOrderMapper.selectExpiredPayingOrdersGlobal(LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        log.info("Found {} expired paying orders to close", expired.size());
        for (PayOrder order : expired) {
            try {
                PayMerchantConfig merchant = resolveMerchantForOrder(order);
                if (merchant != null) {
                    wechatPayGateway.closeOrder(order, merchant);
                }
                order.setStatus("closed");
                order.setClosedAt(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
                payOrderMapper.updateById(order);
                log.info("Closed expired order {}", order.getOrderNo());
            } catch (Exception e) {
                log.warn("Failed to close expired order {}: {}", order.getOrderNo(), e.getMessage());
            }
        }
    }

    private PayMerchantConfig resolveMerchantForOrder(PayOrder order) {
        try {
            if (order.getMerchantConfigId() != null) {
                return paymentMerchantService.requireMerchantById(order.getMerchantConfigId());
            }
            return paymentMerchantService.requireCurrentMerchant();
        } catch (Exception e) {
            log.warn("Cannot resolve merchant for order {}: {}", order.getOrderNo(), e.getMessage());
            return null;
        }
    }
}
