package com.github.sparkzxl.activiti.interfaces.controller.ext;


import com.github.sparkzxl.log.annotation.WebLog;
import com.github.sparkzxl.core.annotation.ResponseResult;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * description: 流程角色管理
 *
 * @author: fin-9062
 * @date: 2021-01-08 17:11:20
 */
@ResponseResult
@RestController
@RequestMapping("/process/role")
@WebLog
@Api(tags = "流程角色管理")
public class ExtProcessRoleController {

}
