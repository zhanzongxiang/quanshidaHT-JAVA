package com.qsd.admin.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("package_prealert")
public class PackagePrealert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String prealertNo;
    private String trackingNo;
    private String courierCode;
    private String warehouseCode;
    private String goodsName;
    private Integer packageCount;
    private BigDecimal estimatedWeight;
    private String remark;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPrealertNo() { return prealertNo; }
    public void setPrealertNo(String prealertNo) { this.prealertNo = prealertNo; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getCourierCode() { return courierCode; }
    public void setCourierCode(String courierCode) { this.courierCode = courierCode; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public Integer getPackageCount() { return packageCount; }
    public void setPackageCount(Integer packageCount) { this.packageCount = packageCount; }
    public BigDecimal getEstimatedWeight() { return estimatedWeight; }
    public void setEstimatedWeight(BigDecimal estimatedWeight) { this.estimatedWeight = estimatedWeight; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
