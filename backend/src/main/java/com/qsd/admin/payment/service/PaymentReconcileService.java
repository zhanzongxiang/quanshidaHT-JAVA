package com.qsd.admin.payment.service;

import com.qsd.admin.payment.dto.WechatReconcileDownloadResult;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayOrder;
import com.qsd.admin.payment.entity.PayReconcileRecord;
import com.qsd.admin.payment.mapper.PayOrderMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentReconcileService {
    private final PayOrderMapper payOrderMapper;
    private final PaymentMerchantService paymentMerchantService;
    private final WechatPayGateway wechatPayGateway;

    public PaymentReconcileService(
        PayOrderMapper payOrderMapper,
        PaymentMerchantService paymentMerchantService,
        WechatPayGateway wechatPayGateway
    ) {
        this.payOrderMapper = payOrderMapper;
        this.paymentMerchantService = paymentMerchantService;
        this.wechatPayGateway = wechatPayGateway;
    }

    public PayReconcileRecord buildTradeBillReconcileRecord(LocalDate billDate, String channel) {
        PayMerchantConfig merchantConfig = paymentMerchantService.requireCurrentMerchant();
        WechatReconcileDownloadResult downloadResult = wechatPayGateway.downloadTradeBill(billDate, merchantConfig);

        Map<String, BillRow> remoteRows = parseTradeBill(downloadResult.content());
        Map<String, PayOrder> localRows = new HashMap<>();
        for (PayOrder order : payOrderMapper.selectByBillDate(billDate)) {
            if (channel.equals(order.getChannel())) {
                localRows.put(order.getOrderNo(), order);
            }
        }

        List<String> diffs = new ArrayList<>();
        for (Map.Entry<String, BillRow> entry : remoteRows.entrySet()) {
            PayOrder order = localRows.remove(entry.getKey());
            if (order == null) {
                diffs.add("missing_local:" + entry.getKey());
                continue;
            }
            BillRow row = entry.getValue();
            int localFen = order.getAmountTotal().movePointRight(2).intValue();
            if (localFen != row.amountFen()) {
                diffs.add("amount_mismatch:" + entry.getKey() + ":local=" + localFen + ",wechat=" + row.amountFen());
            }
        }

        for (String localOnly : localRows.keySet()) {
            diffs.add("missing_remote:" + localOnly);
        }

        PayReconcileRecord record = new PayReconcileRecord();
        record.setReconcileDate(billDate);
        record.setChannel(channel);
        record.setReconcileStatus(diffs.isEmpty() ? "matched" : "diff_found");
        record.setDiffCount(diffs.size());
        record.setSummary(buildSummary(downloadResult, remoteRows.size(), diffs));
        record.setUpdatedAt(LocalDateTime.now());
        return record;
    }

    private Map<String, BillRow> parseTradeBill(String content) {
        Map<String, BillRow> rows = new HashMap<>();
        if (content == null || content.isBlank()) {
            return rows;
        }

        String[] lines = content.split("\\r?\\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("`总交易单数")) {
                continue;
            }
            String[] parts = line.split("`,`");
            if (parts.length < 9) {
                continue;
            }
            String orderNo = clean(parts[3]);
            int amountFen = parseFen(clean(parts[5]));
            String status = clean(parts[8]);
            rows.put(orderNo, new BillRow(orderNo, amountFen, status));
        }
        return rows;
    }

    private String buildSummary(WechatReconcileDownloadResult downloadResult, int remoteCount, List<String> diffs) {
        StringBuilder builder = new StringBuilder();
        builder.append("billDate=").append(downloadResult.billDate())
            .append(",downloadUrl=").append(downloadResult.downloadUrl())
            .append(",remoteCount=").append(remoteCount);
        if (!diffs.isEmpty()) {
            builder.append(",diffs=").append(String.join(" | ", diffs));
        }
        return builder.toString();
    }

    private String clean(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.startsWith("`")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("`")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private int parseFen(String amount) {
        try {
            return new BigDecimal(amount).intValue();
        } catch (Exception ex) {
            return 0;
        }
    }

    private record BillRow(String orderNo, int amountFen, String status) {
    }
}
