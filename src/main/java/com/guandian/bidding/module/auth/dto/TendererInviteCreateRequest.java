package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "邀请招标人")
public class TendererInviteCreateRequest {

    @NotBlank(message = "被邀请人姓名不能为空")
    private String inviteeName;

    @NotBlank(message = "被邀请人手机号不能为空")
    private String inviteePhone;
}
