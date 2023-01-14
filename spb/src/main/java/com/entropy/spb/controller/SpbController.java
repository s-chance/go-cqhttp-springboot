package com.entropy.spb.controller;

import com.entropy.spb.service.SpbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class SpbController {
    @Resource
    private SpbService spbService;

    @PostMapping
    public void SpbEvent(HttpServletRequest request) {
        spbService.SpbEventHandler(request);
    }
}
