package com.qsd.admin.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("member_shipment_package")
public class MemberShipmentPackage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shipmentId;
    private Long packageId;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
}
