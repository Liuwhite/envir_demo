package com.lys.controller;

import com.lys.service.TokenUtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lys
 * @Date 2022/8/16 17:57
 */
@Slf4j
@RestController
public class OrderController {
    @Resource
    private TokenUtilService tokenUtilService;


    /**
     * 获取token字符串测试
     * @return 生成的token
     */
    @GetMapping("/getToken")
    public String getToken(){
        String userInfo = "lys";
        return tokenUtilService.getIdempotentToken(userInfo);
    }


    @PostMapping("/getToken")
    public String addOrder(@RequestHeader(value = "token") String token){
        String userInfo = "lys";
        boolean result = tokenUtilService.validToken(token, userInfo);
        return result ? "正常调用" : "重复调用";
    }

}
