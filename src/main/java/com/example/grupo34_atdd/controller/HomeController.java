package com.example.grupo34_atdd.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/api"})
    public String home() {
        return "redirect:/swagger-ui/index.html";
    }
}
