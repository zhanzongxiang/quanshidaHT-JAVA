package com.qsd.admin.waybill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("waybill_leg")
public class WaybillLeg {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long waybillId;
    private Integer legNo;
    private String legType;
    private String carrierName;
    private String trackingNo;
    private String fromNode;
    private String toNode;
    private String legStatus;
    private Integer transferFlag;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWaybillId() { return waybillId; }
    public void setWaybillId(Long waybillId) { this.waybillId = waybillId; }
    public Integer getLegNo() { return legNo; }
    public void setLegNo(Integer legNo) { this.legNo = legNo; }
    public String getLegType() { return legType; }
    public void setLegType(String legType) { this.legType = legType; }
    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getFromNode() { return fromNode; }
    public void setFromNode(String fromNode) { this.fromNode = fromNode; }
    public String getToNode() { return toNode; }
    public void setToNode(String toNode) { this.toNode = toNode; }
    public String getLegStatus() { return legStatus; }
    public void setLegStatus(String legStatus) { this.legStatus = legStatus; }
    public Integer getTransferFlag() { return transferFlag; }
    public void setTransferFlag(Integer transferFlag) { this.transferFlag = transferFlag; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
