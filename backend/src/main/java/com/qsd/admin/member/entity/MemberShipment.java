package com.qsd.admin.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("member_shipment")
public class MemberShipment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String shipmentNo;
    private Long addressId;
    private Long waybillId;
    private String shipmentStatus;
    private Integer packageCount;
    private BigDecimal totalWeight;
    private String recipientName;
    private String recipientPhone;
    private String destinationCountry;
    private String destinationProvince;
    private String destinationCity;
    private String destinationDistrict;
    private String destinationAddress;
    private String postalCode;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getShipmentNo() { return shipmentNo; }
    public void setShipmentNo(String shipmentNo) { this.shipmentNo = shipmentNo; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public Long getWaybillId() { return waybillId; }
    public void setWaybillId(Long waybillId) { this.waybillId = waybillId; }
    public String getShipmentStatus() { return shipmentStatus; }
    public void setShipmentStatus(String shipmentStatus) { this.shipmentStatus = shipmentStatus; }
    public Integer getPackageCount() { return packageCount; }
    public void setPackageCount(Integer packageCount) { this.packageCount = packageCount; }
    public BigDecimal getTotalWeight() { return totalWeight; }
    public void setTotalWeight(BigDecimal totalWeight) { this.totalWeight = totalWeight; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }
    public String getDestinationProvince() { return destinationProvince; }
    public void setDestinationProvince(String destinationProvince) { this.destinationProvince = destinationProvince; }
    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }
    public String getDestinationDistrict() { return destinationDistrict; }
    public void setDestinationDistrict(String destinationDistrict) { this.destinationDistrict = destinationDistrict; }
    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
