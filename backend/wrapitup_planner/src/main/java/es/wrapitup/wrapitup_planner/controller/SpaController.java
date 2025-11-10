package es.wrapitup.wrapitup_planner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = { "/", "/**/{path:[^\\.]*}" })
    public String forward() {
        // Apunta a la carpeta donde está index.html
        return "forward:/index.html";
    }
}

