package com.qsd.admin.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("pay_reconcile_record")
public class PayReconcileRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate reconcileDate;
    private String channel;
    private String reconcileStatus;
    private Integer diffCount;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getReconcileDate() { return reconcileDate; }
    public void setReconcileDate(LocalDate reconcileDate) { this.reconcileDate = reconcileDate; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getReconcileStatus() { return reconcileStatus; }
    public void setReconcileStatus(String reconcileStatus) { this.reconcileStatus = reconcileStatus; }
    public Integer getDiffCount() { return diffCount; }
    public void setDiffCount(Integer diffCount) { this.diffCount = diffCount; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
