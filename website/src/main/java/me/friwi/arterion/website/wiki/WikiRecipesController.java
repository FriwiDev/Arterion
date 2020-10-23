package me.friwi.arterion.website.wiki;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WikiRecipesController {
    @Autowired
    MessageSource messageSource;

    @GetMapping("/wiki/recipes")
    public String wiki_recipes(Model model) {
        return "wiki/recipes";
    }
}
