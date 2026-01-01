package com.wonder.insight.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class ConnectionCheckerController {

    @GetMapping("/check")
    @SaIgnore
    public String check() {
        return "online";
    }
}
