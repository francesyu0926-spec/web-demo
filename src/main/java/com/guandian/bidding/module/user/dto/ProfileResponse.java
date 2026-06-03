package com.guandian.bidding.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "个人资料")
public class ProfileResponse {

    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private List<String> roles;
    private String activeRole;
}
