package com.github.sparkzxl.activiti.interfaces.controller.ext;


import com.github.sparkzxl.log.annotation.WebLog;
import com.github.sparkzxl.core.annotation.ResponseResult;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * description: 流程用户管理
 *
 * @author: zhouxinlei
 * @date: 2021-01-08 17:12:52
 */
@Api(tags = "流程用户管理")
@WebLog
@ResponseResult
@RestController
@RequestMapping("/process/user")
public class ExtProcessUserController {

}
