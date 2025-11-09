package es.wrapitup.wrapitup_planner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {
    @RequestMapping("/{path:[^\\.]*}")
    public String forwardRoot() {
        return "forward:/index.html";
    }

}
