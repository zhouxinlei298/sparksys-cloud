package com.github.sparkzxl.authorization.domain.model.aggregates;

import lombok.Data;

/**
 * description: 授权回调实体类
 *
 * @author: zhouxinlei
 * @date: 2021-02-26 11:33:51
 */
@Data
public class AuthorizeState {
    /**
     * 登录态
     */
    private String state;

    /**
     * 授权码
     */
    private String code;
}
