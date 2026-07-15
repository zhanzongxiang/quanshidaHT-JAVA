package com.qsd.admin.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PayMerchantConfigCreateRequest(
    @NotBlank(message = "merchantName must not be blank")
    @Size(max = 64, message = "merchantName max length is 64")
    String merchantName,

    @NotBlank(message = "merchantCode must not be blank")
    @Size(max = 64, message = "merchantCode max length is 64")
    String merchantCode,

    @NotBlank(message = "mchId must not be blank")
    @Size(max = 64, message = "mchId max length is 64")
    String mchId,

    @NotBlank(message = "appId must not be blank")
    @Size(max = 64, message = "appId max length is 64")
    String appId,

    @Size(max = 128, message = "appSecret max length is 128")
    String appSecret,

    @NotBlank(message = "notifyUrl must not be blank")
    @Size(max = 255, message = "notifyUrl max length is 255")
    String notifyUrl,

    @Size(max = 128, message = "apiV3Key max length is 128")
    String apiV3Key,

    @Size(max = 255, message = "privateKeyPath max length is 255")
    String privateKeyPath,

    @Size(max = 128, message = "merchantSerialNo max length is 128")
    String merchantSerialNo,

    @Size(max = 255, message = "platformCertificatePath max length is 255")
    String platformCertificatePath,

    Boolean enabled,

    @Size(max = 500, message = "remark max length is 500")
    String remark
) {
}
