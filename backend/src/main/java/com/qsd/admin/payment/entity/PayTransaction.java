package com.qsd.admin.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("pay_transaction")
public class PayTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long payOrderId;
    private String transactionType;
    private String transactionStatus;
    private String requestPayload;
    private String responsePayload;
    private String externalTransactionNo;
    private String externalOutTradeNo;
    private LocalDateTime successTime;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPayOrderId() { return payOrderId; }
    public void setPayOrderId(Long payOrderId) { this.payOrderId = payOrderId; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }
    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }
    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }
    public String getExternalTransactionNo() { return externalTransactionNo; }
    public void setExternalTransactionNo(String externalTransactionNo) { this.externalTransactionNo = externalTransactionNo; }
    public String getExternalOutTradeNo() { return externalOutTradeNo; }
    public void setExternalOutTradeNo(String externalOutTradeNo) { this.externalOutTradeNo = externalOutTradeNo; }
    public LocalDateTime getSuccessTime() { return successTime; }
    public void setSuccessTime(LocalDateTime successTime) { this.successTime = successTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
