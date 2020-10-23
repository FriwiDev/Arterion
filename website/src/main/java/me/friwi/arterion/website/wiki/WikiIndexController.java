package me.friwi.arterion.website.wiki;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WikiIndexController {
    @Autowired
    MessageSource messageSource;

    @GetMapping("/wiki")
    public String wiki() {
        return "redirect:/wiki/index";
    }

    @GetMapping("/wiki/index")
    public String wiki_index(Model model) {
        return "wiki/index";
    }
}
