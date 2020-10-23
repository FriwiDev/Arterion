package me.friwi.arterion.website.wiki;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WikiRulesController {
    @Autowired
    MessageSource messageSource;

    @GetMapping("/wiki/rules")
    public String wiki_rules(Model model) {
        return "wiki/rules";
    }
}
