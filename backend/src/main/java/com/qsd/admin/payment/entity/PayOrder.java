package com.qsd.admin.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("pay_order")
public class PayOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long memberId;
    private Long merchantConfigId;
    private String merchantName;
    private String merchantMchId;
    private String merchantAppId;
    private Long waybillId;
    private String businessType;
    private String sceneType;
    private String channel;
    private String currency;
    private BigDecimal amountTotal;
    private BigDecimal amountPaid;
    private String status;
    private String description;
    private String externalTransactionNo;
    private LocalDateTime paidAt;
    private LocalDateTime expiredAt;
    private LocalDateTime closedAt;
    private LocalDateTime refundedAt;
    private String remark;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getMerchantConfigId() { return merchantConfigId; }
    public void setMerchantConfigId(Long merchantConfigId) { this.merchantConfigId = merchantConfigId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getMerchantMchId() { return merchantMchId; }
    public void setMerchantMchId(String merchantMchId) { this.merchantMchId = merchantMchId; }
    public String getMerchantAppId() { return merchantAppId; }
    public void setMerchantAppId(String merchantAppId) { this.merchantAppId = merchantAppId; }
    public Long getWaybillId() { return waybillId; }
    public void setWaybillId(Long waybillId) { this.waybillId = waybillId; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getSceneType() { return sceneType; }
    public void setSceneType(String sceneType) { this.sceneType = sceneType; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmountTotal() { return amountTotal; }
    public void setAmountTotal(BigDecimal amountTotal) { this.amountTotal = amountTotal; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getExternalTransactionNo() { return externalTransactionNo; }
    public void setExternalTransactionNo(String externalTransactionNo) { this.externalTransactionNo = externalTransactionNo; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
