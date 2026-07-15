package com.qsd.admin.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("member_finance_record")
public class MemberFinanceRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Long orderId;
    private String recordNo;
    private String recordType;
    private BigDecimal amount;
    private String currencyCode;
    private String recordStatus;
    private String note;
    private LocalDateTime createdAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getRecordStatus() { return recordStatus; }
    public void setRecordStatus(String recordStatus) { this.recordStatus = recordStatus; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
