package com.guandian.bidding.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Schema(description = "更新企业资料")
public class EnterpriseUpdateRequest {

    @NotBlank(message = "企业名称不能为空")
    @Size(max = 128, message = "企业名称最长128位")
    private String companyName;

    @NotBlank(message = "统一社会信用代码不能为空")
    @Size(max = 32, message = "信用代码最长32位")
    private String creditCode;

    @Size(max = 64, message = "法人姓名最长64位")
    private String legalPerson;

    @Size(max = 255, message = "地址最长255位")
    private String address;

    @Size(max = 64, message = "联系人姓名最长64位")
    private String contactName;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "联系人手机号格式不正确")
    private String contactPhone;

    @Size(max = 128, message = "银行名称最长128位")
    private String bankName;

    @Size(max = 64, message = "银行账号最长64位")
    private String bankAccount;

    private Long licenseFileId;
}
