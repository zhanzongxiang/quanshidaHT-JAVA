package com.qsd.admin.member.dto;

public record MemberWechatLoginResponse(
    String accessToken,
    String tokenType,
    boolean phoneCompletionRequired,
    String bindTicket
) {
    public static MemberWechatLoginResponse authenticated(String accessToken, String tokenType) {
        return new MemberWechatLoginResponse(accessToken, tokenType, false, null);
    }

    public static MemberWechatLoginResponse phoneCompletionRequired(String bindTicket) {
        return new MemberWechatLoginResponse(null, null, true, bindTicket);
    }
}
