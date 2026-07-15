package com.qsd.admin.member.dto;

public record MemberAddressResponse(
    Long id,
    String contactName,
    String contactPhone,
    String country,
    String province,
    String city,
    String district,
    String detailAddress,
    String postalCode,
    boolean isDefault
) {
}
