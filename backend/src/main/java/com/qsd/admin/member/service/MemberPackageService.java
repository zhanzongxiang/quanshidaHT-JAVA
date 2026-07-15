package com.qsd.admin.member.service;

import com.qsd.admin.member.dto.MemberPackageResponse;
import com.qsd.admin.member.entity.MemberPackage;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberPackageMapper;
import com.qsd.admin.member.mapper.MemberUserMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MemberPackageService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MemberUserMapper memberUserMapper;
    private final MemberPackageMapper memberPackageMapper;

    public MemberPackageService(MemberUserMapper memberUserMapper, MemberPackageMapper memberPackageMapper) {
        this.memberUserMapper = memberUserMapper;
        this.memberPackageMapper = memberPackageMapper;
    }

    public List<MemberPackageResponse> listInventory(String username) {
        MemberUser member = requireMember(username);
        return memberPackageMapper.selectActiveInventoryByMemberId(member.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    private MemberPackageResponse toResponse(MemberPackage item) {
        return new MemberPackageResponse(
            item.getId(),
            item.getPackageNo(),
            item.getTrackingNo(),
            item.getGoodsName(),
            item.getWarehouseCode(),
            item.getPackageCount(),
            item.getWeightKg(),
            item.getPackageStatus(),
            item.getIssueFlag() != null && item.getIssueFlag() == 1,
            item.getIssueType(),
            item.getIssueNote(),
            item.getWarehouseInAt() == null ? null : DATE_TIME_FORMATTER.format(item.getWarehouseInAt())
        );
    }

    private MemberUser requireMember(String username) {
        MemberUser member = memberUserMapper.selectByUsernameOrMobile(username);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }
        return member;
    }
}
