package com.kzdrava.webapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/home")
    public String getHomeView() {
        return "home";
    }

    @GetMapping("/create")
    public String createFolderView() {
        return "create";
    }

}
