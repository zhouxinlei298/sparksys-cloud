package com.github.sparkzxl.oauth.domain.model.aggregates;

import lombok.Data;

/**
 * description: 登录权限
 *
 * @author: zhouxinlei
 * @date: 2020-08-17 11:40:34
 */
@Data
public class ResourceBasicInfo {

    private Long roleId;

    private String code;
    private String name;

}
