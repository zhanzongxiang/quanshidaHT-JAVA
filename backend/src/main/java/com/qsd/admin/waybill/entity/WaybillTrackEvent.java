package com.qsd.admin.waybill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("waybill_track_event")
public class WaybillTrackEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long waybillId;
    private Long legId;
    private LocalDateTime eventTime;
    private String eventStatus;
    private String eventDescription;
    private String eventLocation;
    private Integer visibleToCustomer;
    private Integer sortNo;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWaybillId() { return waybillId; }
    public void setWaybillId(Long waybillId) { this.waybillId = waybillId; }
    public Long getLegId() { return legId; }
    public void setLegId(Long legId) { this.legId = legId; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public String getEventStatus() { return eventStatus; }
    public void setEventStatus(String eventStatus) { this.eventStatus = eventStatus; }
    public String getEventDescription() { return eventDescription; }
    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }
    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }
    public Integer getVisibleToCustomer() { return visibleToCustomer; }
    public void setVisibleToCustomer(Integer visibleToCustomer) { this.visibleToCustomer = visibleToCustomer; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
