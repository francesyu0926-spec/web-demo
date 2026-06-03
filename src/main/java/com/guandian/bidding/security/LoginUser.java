package com.guandian.bidding.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 当前登录用户上下文，存入 SecurityContext。
 * activeRole 为当前激活角色（支持多角色切换）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String activeRole;
}
