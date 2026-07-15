package com.qsd.admin.member.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MemberNumberService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public String nextPrealertNo() {
        return next("PA");
    }

    public String nextPackageNo() {
        return next("PK");
    }

    public String nextShipmentNo() {
        return next("SH");
    }

    public String nextOrderNo() {
        return next("OD");
    }

    public String nextFinanceRecordNo() {
        return next("FR");
    }

    private String next(String prefix) {
        int suffix = ThreadLocalRandom.current().nextInt(100, 1000);
        return prefix + LocalDateTime.now().format(FORMATTER) + suffix;
    }
}
