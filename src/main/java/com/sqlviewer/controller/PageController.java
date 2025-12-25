package com.sqlviewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器
 * 处理页面请求
 */
@Controller
public class PageController {

    /**
     * 首页 - 返回index.html
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 演示页面
     */
    @GetMapping("/demo")
    public String demo() {
        return "demo";
    }
}
