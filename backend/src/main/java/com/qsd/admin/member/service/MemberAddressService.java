package com.qsd.admin.member.service;

import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.member.dto.MemberAddressResponse;
import com.qsd.admin.member.dto.MemberAddressSaveRequest;
import com.qsd.admin.member.entity.MemberAddress;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberAddressMapper;
import com.qsd.admin.member.mapper.MemberUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberAddressService {
    private final MemberUserMapper memberUserMapper;
    private final MemberAddressMapper memberAddressMapper;

    public MemberAddressService(MemberUserMapper memberUserMapper, MemberAddressMapper memberAddressMapper) {
        this.memberUserMapper = memberUserMapper;
        this.memberAddressMapper = memberAddressMapper;
    }

    public List<MemberAddressResponse> list(String username) {
        MemberUser member = requireMember(username);
        return memberAddressMapper.selectByMemberId(member.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public MemberAddressResponse create(String username, MemberAddressSaveRequest request) {
        MemberUser member = requireMember(username);
        if (Boolean.TRUE.equals(request.isDefault())) {
            memberAddressMapper.clearDefaultByMemberId(member.getId());
        }
        MemberAddress address = new MemberAddress();
        address.setMemberId(member.getId());
        apply(address, request);
        memberAddressMapper.insert(address);
        return toResponse(address);
    }

    @Transactional
    public MemberAddressResponse update(String username, Long id, MemberAddressSaveRequest request) {
        MemberUser member = requireMember(username);
        MemberAddress address = memberAddressMapper.selectById(id);
        if (address == null || !address.getMemberId().equals(member.getId())) {
            throw new NotFoundException("地址不存在");
        }
        if (Boolean.TRUE.equals(request.isDefault())) {
            memberAddressMapper.clearDefaultByMemberId(member.getId());
        }
        apply(address, request);
        memberAddressMapper.updateById(address);
        return toResponse(address);
    }

    public void delete(String username, Long id) {
        MemberUser member = requireMember(username);
        MemberAddress address = memberAddressMapper.selectById(id);
        if (address == null || !address.getMemberId().equals(member.getId())) {
            throw new NotFoundException("地址不存在");
        }
        memberAddressMapper.deleteById(id);
    }

    private void apply(MemberAddress address, MemberAddressSaveRequest request) {
        address.setContactName(request.contactName().trim());
        address.setContactPhone(request.contactPhone().trim());
        address.setCountry(blankToDefault(request.country(), "中国"));
        address.setProvince(blankToEmpty(request.province()));
        address.setCity(blankToEmpty(request.city()));
        address.setDistrict(blankToEmpty(request.district()));
        address.setDetailAddress(request.detailAddress().trim());
        address.setPostalCode(blankToEmpty(request.postalCode()));
        address.setIsDefault(Boolean.TRUE.equals(request.isDefault()) ? 1 : 0);
    }

    private MemberAddressResponse toResponse(MemberAddress address) {
        return new MemberAddressResponse(
            address.getId(),
            address.getContactName(),
            address.getContactPhone(),
            address.getCountry(),
            address.getProvince(),
            address.getCity(),
            address.getDistrict(),
            address.getDetailAddress(),
            address.getPostalCode(),
            address.getIsDefault() != null && address.getIsDefault() == 1
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

    private String blankToDefault(String value, String fallback) {
        String trimmed = blankToEmpty(value);
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
