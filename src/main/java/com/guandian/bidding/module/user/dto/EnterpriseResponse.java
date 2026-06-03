package com.guandian.bidding.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "企业资料")
public class EnterpriseResponse {

    private Long id;
    private String companyName;
    private String creditCode;
    private String legalPerson;
    private String address;
    private String contactName;
    private String contactPhone;
    private String bankName;
    private String bankAccount;
    private Long licenseFileId;
    /** 0待认证 1已认证 2驳回 */
    private Integer status;
}
