package io.github.janinduc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")

public class ConnectionCheckerController {

    @GetMapping("/check")
    public String check() {
        return "online";
    }
}
