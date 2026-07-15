package com.qsd.admin.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("member_package")
public class MemberPackage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String packageNo;
    private Long prealertId;
    private String trackingNo;
    private String goodsName;
    private String warehouseCode;
    private Integer packageCount;
    private BigDecimal weightKg;
    private String packageStatus;
    private Integer issueFlag;
    private String issueType;
    private String issueNote;
    private LocalDateTime warehouseInAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPackageNo() { return packageNo; }
    public void setPackageNo(String packageNo) { this.packageNo = packageNo; }
    public Long getPrealertId() { return prealertId; }
    public void setPrealertId(Long prealertId) { this.prealertId = prealertId; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public Integer getPackageCount() { return packageCount; }
    public void setPackageCount(Integer packageCount) { this.packageCount = packageCount; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public String getPackageStatus() { return packageStatus; }
    public void setPackageStatus(String packageStatus) { this.packageStatus = packageStatus; }
    public Integer getIssueFlag() { return issueFlag; }
    public void setIssueFlag(Integer issueFlag) { this.issueFlag = issueFlag; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public String getIssueNote() { return issueNote; }
    public void setIssueNote(String issueNote) { this.issueNote = issueNote; }
    public LocalDateTime getWarehouseInAt() { return warehouseInAt; }
    public void setWarehouseInAt(LocalDateTime warehouseInAt) { this.warehouseInAt = warehouseInAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
