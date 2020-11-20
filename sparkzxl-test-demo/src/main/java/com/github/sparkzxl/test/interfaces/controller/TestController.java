package com.github.sparkzxl.test.interfaces.controller;

import com.github.sparkzxl.core.utils.DateUtils;
import com.github.sparkzxl.file.dto.FileDTO;
import com.github.sparkzxl.log.annotation.WebLog;
import com.github.sparkzxl.test.infrastructure.client.FileClient;
import com.github.sparkzxl.web.annotation.ResponseResult;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/test")
@ResponseResult
@WebLog
@Api(tags = "test管理")
public class TestController {

    private final FileClient fileClient;

    public TestController(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    @GetMapping("/ribbon")
    public String testRibbon(){
        return fileClient.getSayHello();
    }

    @GetMapping("/boolean")
    public boolean testBoolean(){
        return true;
    }

    @GetMapping("/testLocalDateTime")
    public FileDTO testLocalDateTime(){
        FileDTO localDateTime = fileClient.getLocalDateTime();
        System.out.println(DateUtils.formatDate(localDateTime.getLocalDateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return localDateTime;
    }
}