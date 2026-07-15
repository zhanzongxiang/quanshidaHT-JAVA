package com.qsd.admin.member.service;

import com.qsd.admin.member.dto.MemberFinanceRecordResponse;
import com.qsd.admin.member.dto.MemberOrderResponse;
import com.qsd.admin.member.entity.MemberFinanceRecord;
import com.qsd.admin.member.entity.MemberOrder;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberFinanceRecordMapper;
import com.qsd.admin.member.mapper.MemberOrderMapper;
import com.qsd.admin.member.mapper.MemberUserMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MemberOrderService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MemberUserMapper memberUserMapper;
    private final MemberOrderMapper memberOrderMapper;
    private final MemberFinanceRecordMapper memberFinanceRecordMapper;

    public MemberOrderService(
        MemberUserMapper memberUserMapper,
        MemberOrderMapper memberOrderMapper,
        MemberFinanceRecordMapper memberFinanceRecordMapper
    ) {
        this.memberUserMapper = memberUserMapper;
        this.memberOrderMapper = memberOrderMapper;
        this.memberFinanceRecordMapper = memberFinanceRecordMapper;
    }

    public List<MemberOrderResponse> listOrders(String username) {
        MemberUser member = requireMember(username);
        return memberOrderMapper.selectByMemberId(member.getId()).stream()
            .map(this::toOrderResponse)
            .toList();
    }

    public List<MemberFinanceRecordResponse> listFinanceRecords(String username) {
        MemberUser member = requireMember(username);
        return memberFinanceRecordMapper.selectByMemberId(member.getId()).stream()
            .map(this::toFinanceRecordResponse)
            .toList();
    }

    private MemberOrderResponse toOrderResponse(MemberOrder order) {
        return new MemberOrderResponse(
            order.getId(),
            order.getOrderNo(),
            order.getOrderStatus(),
            order.getPaymentStatus(),
            order.getAmount(),
            order.getCurrencyCode(),
            order.getRemark(),
            order.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(order.getCreatedAt())
        );
    }

    private MemberFinanceRecordResponse toFinanceRecordResponse(MemberFinanceRecord record) {
        return new MemberFinanceRecordResponse(
            record.getId(),
            record.getRecordNo(),
            record.getRecordType(),
            record.getAmount(),
            record.getCurrencyCode(),
            record.getRecordStatus(),
            record.getNote(),
            record.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(record.getCreatedAt())
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
