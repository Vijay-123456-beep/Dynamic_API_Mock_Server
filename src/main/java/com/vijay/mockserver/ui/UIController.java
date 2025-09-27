package com.vijay.mockserver.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIController {

    @GetMapping("/")
    public String index() {
        return "redirect:/console";
    }

    @GetMapping("/console")
    public String console() {
        return "console";
    }

    @GetMapping("/console/**")
    public String consoleRoutes() {
        return "console";
    }
}
