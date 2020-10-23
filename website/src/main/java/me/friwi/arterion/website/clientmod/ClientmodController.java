package me.friwi.arterion.website.clientmod;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientmodController {
    @GetMapping("/clientmod")
    public String clientMod() {
        return "clientmod/download";
    }
}
