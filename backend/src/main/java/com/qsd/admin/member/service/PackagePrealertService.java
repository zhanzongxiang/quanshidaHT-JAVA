package com.qsd.admin.member.service;

import com.qsd.admin.member.dto.PackagePrealertResponse;
import com.qsd.admin.member.dto.PackagePrealertSaveRequest;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.entity.PackagePrealert;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.member.mapper.PackagePrealertMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PackagePrealertService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MemberUserMapper memberUserMapper;
    private final PackagePrealertMapper packagePrealertMapper;
    private final MemberNumberService memberNumberService;

    public PackagePrealertService(
        MemberUserMapper memberUserMapper,
        PackagePrealertMapper packagePrealertMapper,
        MemberNumberService memberNumberService
    ) {
        this.memberUserMapper = memberUserMapper;
        this.packagePrealertMapper = packagePrealertMapper;
        this.memberNumberService = memberNumberService;
    }

    public List<PackagePrealertResponse> list(String username) {
        MemberUser member = requireMember(username);
        return packagePrealertMapper.selectByMemberId(member.getId()).stream().map(this::toResponse).toList();
    }

    public PackagePrealertResponse create(String username, PackagePrealertSaveRequest request) {
        MemberUser member = requireMember(username);
        PackagePrealert prealert = new PackagePrealert();
        prealert.setMemberId(member.getId());
        prealert.setPrealertNo(memberNumberService.nextPrealertNo());
        prealert.setTrackingNo(request.trackingNo().trim());
        prealert.setCourierCode(blankToEmpty(request.courierCode()));
        prealert.setWarehouseCode(blankToEmpty(request.warehouseCode()));
        prealert.setGoodsName(request.goodsName().trim());
        prealert.setPackageCount(request.packageCount());
        prealert.setEstimatedWeight(request.estimatedWeight());
        prealert.setRemark(blankToEmpty(request.remark()));
        prealert.setStatus("pending");
        prealert.setCreatedAt(LocalDateTime.now());
        prealert.setUpdatedAt(LocalDateTime.now());
        packagePrealertMapper.insert(prealert);
        return toResponse(prealert);
    }

    private PackagePrealertResponse toResponse(PackagePrealert prealert) {
        return new PackagePrealertResponse(
            prealert.getId(),
            prealert.getPrealertNo(),
            prealert.getTrackingNo(),
            prealert.getCourierCode(),
            prealert.getWarehouseCode(),
            prealert.getGoodsName(),
            prealert.getPackageCount(),
            prealert.getEstimatedWeight(),
            prealert.getRemark(),
            prealert.getStatus(),
            prealert.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(prealert.getCreatedAt())
        );
    }

    private MemberUser requireMember(String username) {
        MemberUser member = memberUserMapper.selectByUsernameOrMobile(username);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }
        return member;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
