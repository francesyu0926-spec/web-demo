package com.guandian.bidding.module.bidder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Schema(description = "投标文件加密/解密")
public class BidDocumentPwdRequest {

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "密码必须为6位数字")
    private String pwd;

    private Long signImgId;
}
